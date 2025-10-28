package org.lgdcloudsim.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HostNetworkTopologyTest {

    private ClosTopologyManager topologyManager;
    private ClosTopology dcTopology;

    @BeforeEach
    void setUp() throws IOException {
        topologyManager = new ClosTopologyManager("src/test/resources/hostTopoConfig.csv");
        dcTopology = topologyManager.getClosTopology(1);
    }

    /**
     * 测试从文件解析和基本拓扑结构
     */
    @Test
    void testParsingAndLookup() throws IOException {
        // Verify DC1 can be retrieved
        ClosTopology dc1 = topologyManager.getClosTopology(1);
        assertNotNull(dc1, "DC1 should exist");
        assertEquals("DC1", dc1.getName());

        // Verify DC2 can be retrieved
        ClosTopology dc2 = topologyManager.getClosTopology(2);
        assertNotNull(dc2, "DC2 should exist");
        assertEquals("DC2", dc2.getName());
    }

    /**
     * 测试拓扑树的层级结构
     */
    @Test
    void testTopologyHierarchy() {
        // 验证DC级别为3
        assertEquals(3, dcTopology.getLevel(), "DC level should be 3");
        assertFalse(dcTopology.isLeafTopology(), "DC should not be leaf");

        // 获取ASW级别(G6)
        List<ClosTopology> aswList = dcTopology.getSubTopologys();
        assertTrue(aswList.size() > 0, "DC should have at least one ASW");
        
        ClosTopology asw = aswList.get(0);
        assertEquals(2, asw.getLevel(), "ASW level should be 2");
        assertFalse(asw.isLeafTopology(), "ASW should not be leaf");

        // 获取PSW级别(P10, P21等)
        List<ClosTopology> pswList = asw.getSubTopologys();
        assertTrue(pswList.size() > 0, "ASW should have at least one PSW");
        
        ClosTopology psw = pswList.get(0);
        assertEquals(1, psw.getLevel(), "PSW level should be 1");
        assertFalse(psw.isLeafTopology(), "PSW should not be leaf");

        // 获取DSW级别(S14, S15等) - 这是叶子节点
        List<ClosTopology> dswList = psw.getSubTopologys();
        assertTrue(dswList.size() > 0, "PSW should have at least one DSW");
        
        ClosTopology dsw = dswList.get(0);
        assertEquals(0, dsw.getLevel(), "DSW level should be 0");
        assertTrue(dsw.isLeafTopology(), "DSW should be leaf");
    }

    /**
     * 测试主机ID范围管理
     */
    @Test
    void testHostIDRange() {
        // 根据hostTopoConfig.csv，DC1包含主机0-399
        assertNotNull(dcTopology.getRange(), "DC topology should have range");
        assertEquals(0, (int) dcTopology.getRange().getMin(), "DC range min should be 0");
        assertEquals(399, (int) dcTopology.getRange().getMax(), "DC range max should be 399");

        // 验证叶子节点的范围
        ClosTopology asw = dcTopology.getSubTopologys().get(0); // G6
        ClosTopology psw = asw.getSubTopologys().get(0); // P10
        List<ClosTopology> dswList = psw.getSubTopologys();
        
        // S14: 0-99, S15: 100-199
        ClosTopology s14 = dswList.stream()
            .filter(t -> "S14".equals(t.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(s14);
        assertEquals(0, (int) s14.getRange().getMin());
        assertEquals(99, (int) s14.getRange().getMax());

        ClosTopology s15 = dswList.stream()
            .filter(t -> "S15".equals(t.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(s15);
        assertEquals(100, (int) s15.getRange().getMin());
        assertEquals(199, (int) s15.getRange().getMax());
    }

    /**
     * 测试子拓扑查询
     */
    @Test
    void testSubTopologyQueries() {
        // 测试isExistSubTopology
        assertTrue(dcTopology.isExistSubTopology("G6"), "G6 should exist in DC1");

        ClosTopology asw = dcTopology.getSubTopologys().stream()
            .filter(t -> "G6".equals(t.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(asw);
        
        assertTrue(asw.isExistSubTopology("P10"), "P10 should exist in G6");
        assertTrue(asw.isExistSubTopology("P21"), "P21 should exist in G6");
        assertFalse(asw.isExistSubTopology("NonExistent"), "NonExistent should not exist");
    }

    /**
     * 测试添加相同的拓扑不会创建重复
     */
    @Test
    void testDuplicateAddition() {
        ClosTopology root = new ClosTopology("root", 3);
        
        // 添加相同的ASW多次
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("A", "A1", "A1b", 10, 19); // 添加到现有的A中
        
        List<ClosTopology> subs = root.getSubTopologys();
        assertEquals(1, subs.size(), "Should only have one A topology");
        
        ClosTopology a = subs.get(0);
        assertEquals(1, a.getSubTopologys().size(), "A should have one PSW (A1)");
        
        ClosTopology a1 = a.getSubTopologys().get(0);
        assertEquals(2, a1.getSubTopologys().size(), "A1 should have two DSWs (A1a and A1b)");
    }

    /**
     * 测试拓扑评分排序
     */
    @Test
    void testTopologyScoreSorting() {
        ClosTopology root = new ClosTopology("root", 2);
        root.AddClosTopology("P1", "D1", 0, 9);
        root.AddClosTopology("P2", "D2", 10, 19);
        root.AddClosTopology("P3", "D3", 20, 29);

        List<ClosTopology> subs = root.getSubTopologys();
        
        // 设置评分
        subs.stream().filter(t -> "P1".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(100));
        subs.stream().filter(t -> "P2".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(50));
        subs.stream().filter(t -> "P3".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(75));

        // 升序排序
        List<ClosTopology> ascending = root.getSortedSubTopologies(true);
        assertEquals("P2", ascending.get(0).getName(), "Ascending order: P2 (50) should be first");
        assertEquals("P3", ascending.get(1).getName(), "Ascending order: P3 (75) should be second");
        assertEquals("P1", ascending.get(2).getName(), "Ascending order: P1 (100) should be last");

        // 降序排序
        List<ClosTopology> descending = root.getSortedSubTopologies(false);
        assertEquals("P1", descending.get(0).getName(), "Descending order: P1 (100) should be first");
        assertEquals("P3", descending.get(1).getName(), "Descending order: P3 (75) should be second");
        assertEquals("P2", descending.get(2).getName(), "Descending order: P2 (50) should be last");
    }

    /**
     * 测试候选副本管理
     */
    @Test
    void testCandidateReplicateManagement() {
        ClosTopology root = new ClosTopology("root", 1);
        root.AddClosTopology("D", 0, 9);
        
        ClosTopology d = root.getSubTopologys().get(0);
        
        // 添加主机和其副本数
        d.AddHost(0, 5);
        d.AddHost(1, 3);
        d.AddHost(5, 2);

        // 测试getCandidateReplicateSum
        assertEquals(10, d.getCandidateReplicateSum(), "Total replicate should be 10");

        // 测试getMostCandidateReplicateHostID
        assertEquals(0, d.getMostCandidateReplicateHostID(), "Host 0 should have most replicates (5)");

        // 测试updateByAllocate
        d.updateByAllocate(0);
        assertEquals(9, d.getCandidateReplicateSum(), "After allocate, total should be 9");
    }

    /**
     * 测试最近公共祖先查询
     */
    @Test
    void testNearestCommonFatherTopology() {
        ClosTopology root = new ClosTopology("root", 3);
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("A", "A1", "A1b", 10, 19);
        root.AddClosTopology("B", "B1", "B1a", 20, 29);

        // 获取叶子节点
        ClosTopology a = root.getSubTopologys().stream()
            .filter(t -> "A".equals(t.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(a);

        ClosTopology a1 = a.getSubTopologys().get(0);
        ClosTopology a1a = a1.getSubTopologys().stream()
            .filter(t -> "A1a".equals(t.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(a1a);

        // 同一个A1a内的两个主机
        List<Integer> hostList = new ArrayList<>();
        hostList.add(0);
        hostList.add(5);
        ClosTopology common = a1a.getNearestCommonFatherTopology(hostList);
        assertNotNull(common);
        assertEquals("A1a", common.getName(), "Common father for hosts in A1a should be A1a");

        // 同一个A1但不同的A1a中的主机
        hostList.clear();
        hostList.add(0);
        hostList.add(15);
        common = a1a.getNearestCommonFatherTopology(hostList);
        assertNotNull(common);
        assertEquals("A1", common.getName(), "Common father for hosts in different A1a should be A1");

        // 同一个根但不同的A和B中的主机
        hostList.clear();
        hostList.add(0);
        hostList.add(25);
        ClosTopology a1a_node = a1.getSubTopologys().stream()
            .filter(t -> "A1a".equals(t.getName()))
            .findFirst()
            .orElse(null);
        assertNotNull(a1a_node);
        common = a1a_node.getNearestCommonFatherTopology(hostList);
        assertNotNull(common);
        assertEquals("root", common.getName(), "Common father for hosts in different subtrees should be root");
    }

    /**
     * 测试DC2的正确解析
     */
    @Test
    void testMultipleDCParsing() {
        ClosTopology dc2 = topologyManager.getClosTopology(2);
        assertNotNull(dc2, "DC2 should exist");
        assertEquals("DC2", dc2.getName());

        // DC2应该只有一个ASW G7
        List<ClosTopology> aswList = dc2.getSubTopologys();
        assertTrue(aswList.stream().anyMatch(t -> "G7".equals(t.getName())), "G7 should exist in DC2");

        // DC2的范围应该是400-499
        assertEquals(400, (int) dc2.getRange().getMin(), "DC2 range min should be 400");
        assertEquals(499, (int) dc2.getRange().getMax(), "DC2 range max should be 499");
    }

    /**
     * 测试clearCandidate功能
     */
    @Test
    void testClearCandidate() {
        ClosTopology root = new ClosTopology("root", 2);
        root.AddClosTopology("P", "D", 0, 9);

        ClosTopology p = root.getSubTopologys().get(0);
        ClosTopology d = p.getSubTopologys().get(0);
        
        // 添加候选主机
        d.AddHost(0, 5);
        d.AddHost(1, 3);
        assertEquals(8, d.getCandidateReplicateSum());

        // 清空候选主机
        root.clearCandidate();
        assertEquals(0, d.getCandidateReplicateSum(), "After clear, candidate should be empty");
    }

    /**
     * 测试特殊主机ID功能
     */
    @Test
    void testSpecialHostID() {
        ClosTopology dsw = new ClosTopology("DSW", 0, 0, 9);
        
        assertEquals(-1, dsw.getSpecialHostID(), "Default special host ID should be -1");
        
        dsw.setSpecialHostID(5);
        assertEquals(5, dsw.getSpecialHostID(), "Special host ID should be set to 5");
    }
}
