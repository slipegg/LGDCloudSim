package org.lgdcloudsim.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClosTopologyTest {

    private ClosTopology root;

    @BeforeEach
    void setUp() {
        root = new ClosTopology("root", 3);
    }

    /**
     * 测试使用三层参数添加拓扑
     */
    @Test
    void testAddClosTopologyAndQueries() {
        // Build a small topology: root -> A -> A1 -> A1a(range 0-9)
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("B", "B1", "B1a", 10, 19);
        root.AddClosTopology("C", "C1", "C1a", 20, 29);

        // root should have A, B, C as direct sub-topologies
        List<ClosTopology> subs = root.getSubTopologys();
        assertEquals(3, subs.size());
        assertTrue(root.isExistSubTopology("A"));
        assertTrue(root.isExistSubTopology("B"));
        assertTrue(root.isExistSubTopology("C"));

        // Verify leaf nodes and ranges
        ClosTopology a = subs.stream().filter(t -> "A".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a);
        ClosTopology a1 = a.getSubTopologys().stream().filter(t -> "A1".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a1);
        ClosTopology a1a = a1.getSubTopologys().stream().filter(t -> "A1a".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a1a);
        assertTrue(a1a.isLeafTopology());
        assertNotNull(a1a.getRange());
        assertEquals(0, (int) a1a.getRange().getMin());
        assertEquals(9, (int) a1a.getRange().getMax());

        // Test isLeafTopology on an intermediate node
        assertFalse(a.isLeafTopology());
    }

    /**
     * 测试getSortedSubTopologies - 升序排序
     */
    @Test
    void testGetSortedSubTopologiesAscending() {
        root.AddClosTopology("P1", "D1", 0, 9);
        root.AddClosTopology("P2", "D2", 10, 19);
        root.AddClosTopology("P3", "D3", 20, 29);

        List<ClosTopology> subs = root.getSubTopologys();
        subs.forEach(t -> t.setTopologyScore(0));
        subs.stream().filter(t -> "P1".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(50));
        subs.stream().filter(t -> "P2".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(10));
        subs.stream().filter(t -> "P3".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(30));

        List<ClosTopology> asc = root.getSortedSubTopologies(true);
        assertEquals("P2", asc.get(0).getName());
        assertEquals("P3", asc.get(1).getName());
        assertEquals("P1", asc.get(2).getName());
    }

    /**
     * 测试getSortedSubTopologies - 降序排序
     */
    @Test
    void testGetSortedSubTopologiesDescending() {
        root.AddClosTopology("P1", "D1", 0, 9);
        root.AddClosTopology("P2", "D2", 10, 19);
        root.AddClosTopology("P3", "D3", 20, 29);

        List<ClosTopology> subs = root.getSubTopologys();
        subs.forEach(t -> t.setTopologyScore(0));
        subs.stream().filter(t -> "P1".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(50));
        subs.stream().filter(t -> "P2".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(10));
        subs.stream().filter(t -> "P3".equals(t.getName())).findFirst().ifPresent(t -> t.setTopologyScore(30));

        List<ClosTopology> desc = root.getSortedSubTopologies(false);
        assertEquals("P1", desc.get(0).getName());
        assertEquals("P3", desc.get(1).getName());
        assertEquals("P2", desc.get(2).getName());
    }

    /**
     * 测试使用二层参数添加拓扑
     */
    @Test
    void testAddClosTopologyTwoLevels() {
        ClosTopology root2 = new ClosTopology("root2", 2);
        root2.AddClosTopology("P", "D", 0, 9);
        root2.AddClosTopology("P", "D2", 10, 19); // 添加到现有的P中

        List<ClosTopology> pList = root2.getSubTopologys();
        assertEquals(1, pList.size());

        ClosTopology p = pList.get(0);
        assertEquals("P", p.getName());
        assertEquals(1, p.getLevel());

        List<ClosTopology> dList = p.getSubTopologys();
        assertEquals(2, dList.size());
    }

    /**
     * 测试使用一层参数添加拓扑
     */
    @Test
    void testAddClosTopologyOneLevel() {
        ClosTopology root1 = new ClosTopology("root1", 1);
        root1.AddClosTopology("D1", 0, 9);
        root1.AddClosTopology("D2", 10, 19);
        root1.AddClosTopology("D3", 20, 29);

        List<ClosTopology> dList = root1.getSubTopologys();
        assertEquals(3, dList.size());

        ClosTopology d1 = dList.stream().filter(t -> "D1".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(d1);
        assertTrue(d1.isLeafTopology());
        assertEquals(0, d1.getLevel());
    }

    /**
     * 测试范围自动更新
     */
    @Test
    void testRangeAutomaticUpdate() {
        root.AddClosTopology("A", "A1", "A1a", 0, 49);
        root.AddClosTopology("A", "A1", "A1b", 50, 99);
        root.AddClosTopology("B", "B1", "B1a", 100, 149);

        // 验证root的范围正确合并
        assertEquals(0, (int) root.getRange().getMin());
        assertEquals(149, (int) root.getRange().getMax());

        // 验证A的范围
        ClosTopology a = root.getSubTopologys().stream().filter(t -> "A".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a);
        assertEquals(0, (int) a.getRange().getMin());
        assertEquals(99, (int) a.getRange().getMax());

        // 验证B的范围
        ClosTopology b = root.getSubTopologys().stream().filter(t -> "B".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(b);
        assertEquals(100, (int) b.getRange().getMin());
        assertEquals(149, (int) b.getRange().getMax());
    }

    /**
     * 测试候选副本功能
     */
    @Test
    void testCandidateReplicates() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        
        ClosTopology a = root.getSubTopologys().get(0);
        ClosTopology a1 = a.getSubTopologys().get(0);
        ClosTopology a1a = a1.getSubTopologys().get(0);

        // 添加候选副本
        a1a.AddHost(0, 5);
        a1a.AddHost(1, 3);
        a1a.AddHost(2, 2);

        // 测试getCandidateReplicateSum
        assertEquals(10, a1a.getCandidateReplicateSum());

        // 测试getMostCandidateReplicateHostID
        assertEquals(0, a1a.getMostCandidateReplicateHostID());

        // 测试updateByAllocate
        a1a.updateByAllocate(0);
        assertEquals(9, a1a.getCandidateReplicateSum());
        assertEquals(0, a1a.getMostCandidateReplicateHostID());
    }

    /**
     * 测试clearCandidate功能
     */
    @Test
    void testClearCandidate() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("B", "B1", "B1a", 10, 19);

        ClosTopology a = root.getSubTopologys().get(0);
        ClosTopology a1 = a.getSubTopologys().get(0);
        ClosTopology a1a = a1.getSubTopologys().get(0);

        ClosTopology b = root.getSubTopologys().get(1);
        ClosTopology b1 = b.getSubTopologys().get(0);
        ClosTopology b1a = b1.getSubTopologys().get(0);

        a1a.AddHost(0, 5);
        a1a.AddHost(1, 3);
        b1a.AddHost(10, 7);
        b1a.AddHost(11, 2);

        assertEquals(8, a1a.getCandidateReplicateSum());
        assertEquals(9, b1a.getCandidateReplicateSum());

        // 清空所有候选副本
        root.clearCandidate();

        assertEquals(0, a1a.getCandidateReplicateSum());
        assertEquals(0, b1a.getCandidateReplicateSum());
    }

    /**
     * 测试isExistSubTopology功能
     */
    @Test
    void testIsExistSubTopology() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("B", "B1", "B1a", 10, 19);

        ClosTopology a = root.getSubTopologys().stream().filter(t -> "A".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a);

        assertTrue(a.isExistSubTopology("A1"));
        assertFalse(a.isExistSubTopology("A2"));
        assertFalse(a.isExistSubTopology("B1"));
    }

    /**
     * 测试special host ID功能
     */
    @Test
    void testSpecialHostID() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);

        ClosTopology a = root.getSubTopologys().get(0);
        ClosTopology a1 = a.getSubTopologys().get(0);
        ClosTopology a1a = a1.getSubTopologys().get(0);

        assertEquals(-1, a1a.getSpecialHostID(), "Default special host ID should be -1");

        a1a.setSpecialHostID(5);
        assertEquals(5, a1a.getSpecialHostID());

        a1a.setSpecialHostID(0);
        assertEquals(0, a1a.getSpecialHostID());
    }

    /**
     * 测试复杂的嵌套结构和范围
     */
    @Test
    void testComplexNestedStructure() {
        root.AddClosTopology("A", "A1", "A1a", 0, 24);
        root.AddClosTopology("A", "A1", "A1b", 25, 49);
        root.AddClosTopology("A", "A2", "A2a", 50, 74);
        root.AddClosTopology("A", "A2", "A2b", 75, 99);

        ClosTopology a = root.getSubTopologys().stream().filter(t -> "A".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a);
        assertEquals(2, a.getSubTopologys().size()); // A1 和 A2

        ClosTopology a1 = a.getSubTopologys().stream().filter(t -> "A1".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a1);
        assertEquals(2, a1.getSubTopologys().size()); // A1a 和 A1b
        assertEquals(0, (int) a1.getRange().getMin());
        assertEquals(49, (int) a1.getRange().getMax());

        ClosTopology a2 = a.getSubTopologys().stream().filter(t -> "A2".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a2);
        assertEquals(2, a2.getSubTopologys().size()); // A2a 和 A2b
        assertEquals(50, (int) a2.getRange().getMin());
        assertEquals(99, (int) a2.getRange().getMax());

        // 验证root的总范围
        assertEquals(0, (int) root.getRange().getMin());
        assertEquals(99, (int) root.getRange().getMax());
    }

    /**
     * 测试getNearestCommonFatherTopology功能
     */
    @Test
    void testGetNearestCommonFatherTopology() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("A", "A1", "A1b", 10, 19);
        root.AddClosTopology("B", "B1", "B1a", 20, 29);

        ClosTopology a = root.getSubTopologys().stream().filter(t -> "A".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a);

        ClosTopology a1 = a.getSubTopologys().get(0);
        ClosTopology a1a = a1.getSubTopologys().stream().filter(t -> "A1a".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(a1a);

        // 同一叶子节点内的主机
        List<Integer> hostList = new ArrayList<>();
        hostList.add(0);
        hostList.add(5);
        ClosTopology common = a1a.getNearestCommonFatherTopology(hostList);
        assertNotNull(common);
        assertEquals("A1a", common.getName());

        // 同一中间节点下的不同叶子节点中的主机
        hostList.clear();
        hostList.add(0);
        hostList.add(15);
        common = a1a.getNearestCommonFatherTopology(hostList);
        assertNotNull(common);
        assertEquals("A1", common.getName());

        // 不同顶级节点下的主机
        hostList.clear();
        hostList.add(0);
        hostList.add(25);
        common = a1a.getNearestCommonFatherTopology(hostList);
        assertNotNull(common);
        assertEquals("root", common.getName());
    }

    /**
     * 测试toString方法
     */
    @Test
    void testToString() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.setTopologyScore(100);

        String str = root.toString();
        assertNotNull(str);
        assertTrue(str.contains("root"));
        assertTrue(str.contains("100"));
        assertTrue(str.contains("level=3"));
    }

    /**
     * 测试拓扑重复添加
     */
    @Test
    void testDuplicateTopologyAddition() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);
        root.AddClosTopology("A", "A1", "A1a", 0, 9); // 添加相同的

        List<ClosTopology> subs = root.getSubTopologys();
        assertEquals(1, subs.size(), "Should not create duplicate");
    }

    /**
     * 测试父子关系的维护
     */
    @Test
    void testParentChildRelationship() {
        root.AddClosTopology("A", "A1", "A1a", 0, 9);

        ClosTopology a = root.getSubTopologys().get(0);
        assertEquals(root, a.getFatherTopology(), "A's father should be root");

        ClosTopology a1 = a.getSubTopologys().get(0);
        assertEquals(a, a1.getFatherTopology(), "A1's father should be A");

        ClosTopology a1a = a1.getSubTopologys().get(0);
        assertEquals(a1, a1a.getFatherTopology(), "A1a's father should be A1");
    }

    /**
     * 测试大规模拓扑构建
     */
    @Test
    void testLargeScaleTopologyConstruction() {
        // 构建有多个分支的大规模拓扑
        for (int i = 0; i < 5; i++) {
            final int aswIndex = i;
            String aswName = "ASW" + aswIndex;
            for (int j = 0; j < 3; j++) {
                String pswName = "PSW" + aswIndex + "_" + j;
                int startID = aswIndex * 100 + j * 30;
                int endID = startID + 29;
                root.AddClosTopology(aswName, pswName, pswName + "_DSW", startID, endID);
            }
        }

        List<ClosTopology> aswList = root.getSubTopologys();
        assertEquals(5, aswList.size(), "Should have 5 ASWs");

        // 验证范围正确
        assertEquals(0, (int) root.getRange().getMin());
        assertEquals(489, (int) root.getRange().getMax());

        // 验证每个ASW
        for (int i = 0; i < 5; i++) {
            final int aswIndex = i;
            ClosTopology asw = aswList.stream().filter(t -> ("ASW" + aswIndex).equals(t.getName())).findFirst().orElse(null);
            assertNotNull(asw);
            assertEquals(3, asw.getSubTopologys().size());
        }
    }
}
