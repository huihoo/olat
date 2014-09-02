package org.olat.lms.monitoring;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.data.commons.database.DBQueryImpl;
import org.olat.data.commons.database.SimpleProbe;

public class SimpleProbeServiceImplTest {

    private SimpleProbeService simpleProbeService;
    private SimpleProbeBusinessObject simpleProbeBOMock;
    private DBQueryImpl dbQueryImpl;

    @Before
    public void setUp() throws Exception {

        simpleProbeService = new SimpleProbeServiceImpl();
        simpleProbeBOMock = new SimpleProbeBusinessObjectImpl();
        ((SimpleProbeServiceImpl) simpleProbeService).simpleProbeBOImpl = simpleProbeBOMock;

    }

    /**
     * tests if returned object correspond to key parameter
     */
    @Test
    public void getSimpleProbe() {

        createListTableStatsMap(2);
        SimpleProbeObject simpleProbeTO = simpleProbeService.getSimpleProbe("org.olat.data.group.area.BGAreaImpl");
        assertEquals("org.olat.data.group.area.BGAreaImpl", simpleProbeTO.getKey());

    }

    /**
     * test if returned list contains all nonregistered elements
     */
    @Test
    public void getSimpleProbeList() {

        createListTableStatsMap(3);

        List<SimpleProbeObject> simpleProbeTOList = simpleProbeService.getSimpleProbeNonRegisteredList();
        assertEquals(1, simpleProbeTOList.size());

    }

    /**
     * test if returned list of nonregistered elements contains right sum value
     */
    @Test
    public void getSimpleProbeListCheckSum() {

        createListTableStatsMap(3);

        List<SimpleProbeObject> simpleProbeTOList = simpleProbeService.getSimpleProbeNonRegisteredList();
        assertEquals(12, ((SimpleProbeObject) simpleProbeTOList.get(0)).getTotalSum());

    }

    private void createListTableStatsMap(int numberOfMapEntries) {

        /* dummy */
        dbQueryImpl = new DBQueryImpl(null);

        switch (numberOfMapEntries) {
        case 3:
            SimpleProbe mockSimpleProbe3 = new SimpleProbe();
            mockSimpleProbe3.addMeasurement(3);
            mockSimpleProbe3.addMeasurement(4);
            mockSimpleProbe3.addMeasurement(5);
            dbQueryImpl.listTableStatsMap_.put("THEREST", mockSimpleProbe3);
        case 2:
            SimpleProbe mockSimpleProbe2 = new SimpleProbe();
            mockSimpleProbe2.addMeasurement(2);
            mockSimpleProbe2.addMeasurement(3);
            mockSimpleProbe2.addMeasurement(4);
            dbQueryImpl.listTableStatsMap_.put("org.olat.data.basesecurity.SecurityGroupMembershipImpl", mockSimpleProbe2);

        case 1:
        default:
            SimpleProbe mockSimpleProbe1 = new SimpleProbe();
            mockSimpleProbe1.addMeasurement(1);
            mockSimpleProbe1.addMeasurement(2);
            dbQueryImpl.listTableStatsMap_.put("org.olat.data.group.area.BGAreaImpl", mockSimpleProbe1);
        }

    }
}
