package org.lgdcloudsim.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClosTopologyManagerTest {

    private ClosTopologyManager topologyManager;

    @BeforeEach
    void setUp() throws IOException {
        topologyManager = new ClosTopologyManager("src/test/resources/hostTopoConfig.csv");
    }

    /**
     * 测试从CSV文件解析拓扑结构
     */
    @Test
    void testParsingAndStructure() throws IOException {
        // Verify DC1 exists and has expected structure
        ClosTopology dc1 = topologyManager.getClosTopology(1);
        assertNotNull(dc1, "DC1 topology should be present");
        assertEquals("DC1", dc1.getName());

        // root should have one ASW named G6 for DC1
        List<ClosTopology> s2s = dc1.getSubTopologys();
        assertTrue(s2s.stream().anyMatch(t -> "G6".equals(t.getName())), "G6 should be a sub-topology of DC1");

        ClosTopology g6 = s2s.stream().filter(t -> "G6".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(g6);

        // G6 should contain PSW nodes P10 and P21
        assertTrue(g6.getSubTopologys().stream().anyMatch(t -> "P10".equals(t.getName())));
        assertTrue(g6.getSubTopologys().stream().anyMatch(t -> "P21".equals(t.getName())));

        ClosTopology p10 = g6.getSubTopologys().stream().filter(t -> "P10".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(p10);

        // P10 should have leaf DSW nodes S14 and S15
        assertTrue(p10.getSubTopologys().stream().anyMatch(t -> "S14".equals(t.getName())));
        assertTrue(p10.getSubTopologys().stream().anyMatch(t -> "S15".equals(t.getName())));

        ClosTopology s14 = p10.getSubTopologys().stream().filter(t -> "S14".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s14);
        assertTrue(s14.isLeafTopology(), "S14 should be a leaf topology");
        assertNotNull(s14.getRange(), "Leaf topology should have a range");
        assertEquals(0, (int) s14.getRange().getMin());
        assertEquals(99, (int) s14.getRange().getMax());

        // Verify DC2 parsing
        ClosTopology dc2 = topologyManager.getClosTopology(2);
        assertNotNull(dc2);
        assertEquals("DC2", dc2.getName());
        assertTrue(dc2.getSubTopologys().stream().anyMatch(t -> "G7".equals(t.getName())));
    }

    /**
     * 测试多个数据中心的加载
     */
    @Test
    void testMultipleDCLoading() {
        ClosTopology dc1 = topologyManager.getClosTopology(1);
        ClosTopology dc2 = topologyManager.getClosTopology(2);

        assertNotNull(dc1);
        assertNotNull(dc2);
        assertNotEquals(dc1.getName(), dc2.getName());

        // DC1 有两个ASW (G6)，DC2 有一个ASW (G7)
        assertEquals(1, dc1.getSubTopologys().size()); // 只有G6
        assertEquals(1, dc2.getSubTopologys().size()); // 只有G7

        // DC1的范围是0-399，DC2的范围是400-499
        assertEquals(0, (int) dc1.getRange().getMin());
        assertEquals(399, (int) dc1.getRange().getMax());
        assertEquals(400, (int) dc2.getRange().getMin());
        assertEquals(499, (int) dc2.getRange().getMax());
    }

    /**
     * 测试DC1内部结构详细验证
     */
    @Test
    void testDC1Structure() {
        ClosTopology dc1 = topologyManager.getClosTopology(1);
        assertEquals(3, dc1.getLevel(), "DC level should be 3");

        // 获取G6 (ASW)
        ClosTopology g6 = dc1.getSubTopologys().get(0);
        assertEquals(2, g6.getLevel(), "G6 level should be 2");
        assertEquals("G6", g6.getName());

        // 验证G6下的PSW
        List<ClosTopology> pswList = g6.getSubTopologys();
        assertEquals(2, pswList.size(), "G6 should have 2 PSWs (P10, P21)");

        ClosTopology p10 = pswList.stream().filter(t -> "P10".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(p10);
        assertEquals(1, p10.getLevel(), "P10 level should be 1");

        // 验证P10下的DSW
        List<ClosTopology> dswList = p10.getSubTopologys();
        assertEquals(2, dswList.size(), "P10 should have 2 DSWs (S14, S15)");

        ClosTopology s14 = dswList.stream().filter(t -> "S14".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s14);
        assertEquals(0, s14.getLevel(), "S14 level should be 0");
        assertTrue(s14.isLeafTopology(), "S14 should be leaf");
    }

    /**
     * 测试DC2内部结构详细验证
     */
    @Test
    void testDC2Structure() {
        ClosTopology dc2 = topologyManager.getClosTopology(2);
        assertEquals(3, dc2.getLevel(), "DC2 level should be 3");

        // 获取G7 (ASW)
        ClosTopology g7 = dc2.getSubTopologys().get(0);
        assertEquals(2, g7.getLevel(), "G7 level should be 2");
        assertEquals("G7", g7.getName());

        // 验证G7下的PSW
        List<ClosTopology> pswList = g7.getSubTopologys();
        assertEquals(1, pswList.size(), "G7 should have 1 PSW (P31)");

        ClosTopology p31 = pswList.get(0);
        assertEquals("P31", p31.getName());
        assertEquals(1, p31.getLevel(), "P31 level should be 1");

        // 验证P31下的DSW
        List<ClosTopology> dswList = p31.getSubTopologys();
        assertEquals(1, dswList.size(), "P31 should have 1 DSW (S32)");

        ClosTopology s32 = dswList.get(0);
        assertEquals("S32", s32.getName());
        assertEquals(0, s32.getLevel(), "S32 level should be 0");
        assertTrue(s32.isLeafTopology(), "S32 should be leaf");
        assertEquals(400, (int) s32.getRange().getMin());
        assertEquals(499, (int) s32.getRange().getMax());
    }

    /**
     * 测试非存在的DC返回null
     */
    @Test
    void testNonExistentDC() {
        ClosTopology dc99 = topologyManager.getClosTopology(99);
        assertNull(dc99, "Non-existent DC should return null");
    }

    /**
     * 测试所有叶子节点的范围正确性
     */
    @Test
    void testLeafNodeRanges() {
        ClosTopology dc1 = topologyManager.getClosTopology(1);
        
        // DC1 的所有叶子节点范围: S14(0-99), S15(100-199), S21(200-299), S22(300-399)
        List<ClosTopology> aswList = dc1.getSubTopologys();
        assertFalse(aswList.isEmpty());

        ClosTopology g6 = aswList.get(0);
        List<ClosTopology> pswList = g6.getSubTopologys();

        // 找P10
        ClosTopology p10 = pswList.stream().filter(t -> "P10".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(p10);

        List<ClosTopology> dswList = p10.getSubTopologys();
        assertEquals(2, dswList.size());

        ClosTopology s14 = dswList.stream().filter(t -> "S14".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s14);
        assertEquals(0, (int) s14.getRange().getMin());
        assertEquals(99, (int) s14.getRange().getMax());

        ClosTopology s15 = dswList.stream().filter(t -> "S15".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s15);
        assertEquals(100, (int) s15.getRange().getMin());
        assertEquals(199, (int) s15.getRange().getMax());

        // 找P21
        ClosTopology p21 = pswList.stream().filter(t -> "P21".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(p21);

        List<ClosTopology> dswListP21 = p21.getSubTopologys();
        assertEquals(2, dswListP21.size());

        ClosTopology s21 = dswListP21.stream().filter(t -> "S21".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s21);
        assertEquals(200, (int) s21.getRange().getMin());
        assertEquals(299, (int) s21.getRange().getMax());

        ClosTopology s22 = dswListP21.stream().filter(t -> "S22".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s22);
        assertEquals(300, (int) s22.getRange().getMin());
        assertEquals(399, (int) s22.getRange().getMax());
    }

    /**
     * 测试拓扑树的父子关系
     */
    @Test
    void testParentChildRelationship() {
        ClosTopology dc1 = topologyManager.getClosTopology(1);

        ClosTopology g6 = dc1.getSubTopologys().get(0);
        assertEquals(dc1, g6.getFatherTopology(), "G6's father should be DC1");

        ClosTopology p10 = g6.getSubTopologys().stream().filter(t -> "P10".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(p10);
        assertEquals(g6, p10.getFatherTopology(), "P10's father should be G6");

        ClosTopology s14 = p10.getSubTopologys().stream().filter(t -> "S14".equals(t.getName())).findFirst().orElse(null);
        assertNotNull(s14);
        assertEquals(p10, s14.getFatherTopology(), "S14's father should be P10");
    }
}
