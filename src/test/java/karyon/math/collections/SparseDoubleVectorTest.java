package karyon.math.collections;

import karyon.IRunnable;
import karyon.exceptions.InvalidParameterException;
import karyon.testing.KaryonTest;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: kmchugh
 * Date: 25/7/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SparseDoubleVectorTest
        extends KaryonTest
{
    @Test
    public void testConstructor() throws Exception
    {
        startMarker();
        SparseDoubleVector loVec = new SparseDoubleVector();
        assertNotNull(loVec);
    }

    @Test
    public void testConstructor_long_float_boolean() throws Exception
    {
        startMarker();
        assertWillThrow(InvalidParameterException.class, new IRunnable() {
            @Override
            public void run() throws Throwable
            {
                SparseDoubleVector loVec = new SparseDoubleVector(0, .75f, true);
            }
        });

        assertWillThrow(InvalidParameterException.class, new IRunnable() {
            @Override
            public void run() throws Throwable
            {
                SparseDoubleVector loVec = new SparseDoubleVector(10, 1.5f, true);
            }
        });

        SparseDoubleVector loVec = new SparseDoubleVector(10, .75f, true);
        assertTrue(loVec.isHorizontal());

        loVec = new SparseDoubleVector(10, .75f, false);
        assertFalse(loVec.isHorizontal());
    }

    @Test
    public void testConstructor_array() throws Exception
    {
        startMarker();
        double[] laDouble = new double[]{1,2,3,4,5};
        SparseDoubleVector loVec = new SparseDoubleVector(laDouble);

        assertEquals(laDouble.length, loVec.size());
        for(int i=0; i<laDouble.length; i++)
        {
            assertEquals(laDouble[i], loVec.getDouble(i), 0);
        }
    }

    @Test
    public void testConstructor_array_boolean() throws Exception
    {
        startMarker();
        double[] laDouble = new double[]{1,2,3,4,5};
        SparseDoubleVector loVec = new SparseDoubleVector(laDouble, true);
        assertTrue(loVec.isHorizontal());

        loVec = new SparseDoubleVector(laDouble, false);
        assertFalse(loVec.isHorizontal());
    }

    @Test
    public void testIsHorizontal() throws Exception
    {
        startMarker();
        assertTrue(new SparseDoubleVector(new double[0], true).isHorizontal());
        assertFalse(new SparseDoubleVector(new double[0], false).isHorizontal());
    }

    @Test
    public void testGetCapacity() throws Exception
    {
        startMarker();
        assertEquals(10, new SparseDoubleVector(10,0.75f, true).getCapacity());
        assertEquals(20, new SparseDoubleVector(20,0.75f, true).getCapacity());
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        startMarker();
        assertTrue(new SparseDoubleVector().isEmpty());
        assertTrue(new SparseDoubleVector(new double[0]).isEmpty());
        assertTrue(new SparseDoubleVector(new double[]{}).isEmpty());
        assertFalse(new SparseDoubleVector(new double[]{0, 1, 2, 3}).isEmpty());
    }

    @Test
    public void testAddDouble() throws Exception
    {
        startMarker();
        double[] laDouble = new double[]{1,2,3,4,5};
        SparseDoubleVector loVec = new SparseDoubleVector(laDouble);
        assertEquals(5, loVec.size());
        loVec.addDouble(6);
        assertEquals(6, loVec.size());
        loVec.addDouble(7);
        assertEquals(7, loVec.size());
        loVec.addDouble(8);
        assertEquals(8, loVec.size());
        loVec.addDouble(9);
        assertEquals(9, loVec.size());

        for(int i=0; i<9; i++)
        {
            assertEquals(i+1, loVec.getDouble(i), 0);
        }
    }

    @Test
    public void testSize() throws Exception
    {
        startMarker();
        SparseDoubleVector loVec = new SparseDoubleVector();
        assertEquals(0, loVec.size());
        loVec.addDouble(1);
        assertEquals(1, loVec.size());
        double[] laDouble = new double[]{1,2,3,4,5};
        loVec.addAllDouble(laDouble);
        assertEquals(6, loVec.size());

        loVec.addDouble(100, 1);
        assertEquals(101, loVec.size());
        loVec.addDouble(2);
        assertEquals(102, loVec.size());
    }

    @Test
    public void testCount() throws Exception
    {
        startMarker();

        SparseDoubleVector loVec = new SparseDoubleVector();
        assertEquals(0, loVec.count());
        loVec.addDouble(1);
        assertEquals(1, loVec.count());
        double[] laDouble = new double[]{1,2,3,4,5};
        loVec.addAllDouble(laDouble);
        assertEquals(6, loVec.count());

        loVec.addDouble(100, 1);
        assertEquals(7, loVec.count());
        loVec.addDouble(2);
        assertEquals(8, loVec.count());
    }

    @Test
    public void testAddAllDouble() throws Exception
    {
        startMarker();
        double[] laDouble = new double[]{1,2,3,4,5};
        SparseDoubleVector loVec = new SparseDoubleVector();
        assertEquals(0, loVec.size());

        loVec.addAllDouble(laDouble);
        assertEquals(5, loVec.size());

        loVec.addAllDouble(laDouble);
        assertEquals(10, loVec.size());

        for(int i=0; i<loVec.size(); i++)
        {
            assertEquals(laDouble[i%5], loVec.getDouble(i), 0);
        }
    }
}
