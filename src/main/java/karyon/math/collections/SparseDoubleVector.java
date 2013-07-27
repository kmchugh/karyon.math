package karyon.math.collections;

import karyon.collections.IList;
import karyon.exceptions.InvalidParameterException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Sparse Double Vector is a collection of doubles organised as a single vector
 * This could be horizontal or vertically based.
 *
 * A horizontal vector would be represented as:
 * [0,1,2,3,4,5]
 * whereas a vertical vector would be represented as:
 * [0,
 *  1,
 *  2,
 *  3,
 *  4,
 *  5]
 *
 *  A Vector has an initial capacity and a fill factory.
 *  When the contents of the vector reach the capacity,
 *  the internal storage mechanism will be extended
 *  by fill factor.  This expansion mechanism may
 *  take time so if vectors are going to be large
 *  they should be initialised with a large capacity
 *
 *  The initial capacity is always > 0
 *  The fill factor is always 0 < fill factor < 1
 *
 *  If the contents of the internal storage are < (capacity * fill factor)
 *  the internal storage will be reduced by fill factor
 *
 */
public class SparseDoubleVector
    extends karyon.Object
    implements IList<Double>
{
    private class ArrayMarker
    {
        private int m_nDataIndex;
        private int m_nItemIndex;
        private int m_nStartIndex;
        private int m_nEndIndex;

        /**
         * Creates a new ArrayMarker
         * @param tnDataIndex the index of the list of list of doubles (internal list pointer)
         * @param tnStartIndex the index that the secondary array index starts at (reference index)
         */
        public ArrayMarker(int tnDataIndex, int tnItemIndex, int tnStartIndex)
        {
            m_nDataIndex = tnDataIndex;
            m_nStartIndex = tnStartIndex;
            m_nEndIndex = tnStartIndex;
            m_nItemIndex = tnItemIndex;
        }

        /**
         * Gets the array with the data contained by this marker
         * @return the array with the data contained by this marker
         */
        public double[] getArray()
        {
            return m_aData[m_nDataIndex];
        }

        /**
         * Appends the specified value to the end of the array marker
         * @param tnValue the value to add
         * @return true if the value was added
         */
        public synchronized boolean add(double tnValue)
        {
            double[] laArray = getArray();
            laArray[(m_nEndIndex++ - m_nStartIndex) + m_nItemIndex] = tnValue;
            if (m_nEndIndex - m_nStartIndex + m_nItemIndex == laArray.length)
            {
                // Need to resize the array
                m_aData[m_nDataIndex] = java.util.Arrays.copyOf(laArray, (int)(Math.ceil(laArray.length / m_nFillFactor)));
                m_nCapacity = m_aData[m_nDataIndex].length;
            }
            return true;
        }

        /**
         * Gets the full capacity available to this marker
         * @return the capacity available to this marker
         */
        public int getCapacity()
        {
            return getArray().length - m_nStartIndex;
        }

        /**
         * Appends the value at the specified index.  This will result
         * in a shift of all values after tnIndex
         * @param tnIndex the index to append at
         * @param tnValue the value to add
         */
        public synchronized void add(long tnIndex, double tnValue)
        {
            int lnOffset = (int)(tnIndex - m_nStartIndex);
            double[] laArray = getArray();
            java.lang.System.arraycopy(laArray, lnOffset, laArray, lnOffset +1, laArray.length-lnOffset-1);
            laArray[lnOffset]=tnValue;
        }

        /**
         * Gets the value at the index specified, if the index is out of bounds, returns 0
         * @param tnIndex the index to get the value of
         * @return the value at the specified index, or 0 if the index did not exist
         */
        public double get(long tnIndex)
        {
            int lnOffset = (int)(tnIndex - m_nStartIndex);
            double[] laArray = getArray();
            return laArray.length > lnOffset ? laArray[lnOffset] : 0;
        }

        public double set(long tnIndex, double tnValue)
        {
            int lnOffset = (int)(tnIndex - m_nStartIndex);
            double[] laArray = getArray();
            if (laArray.length > lnOffset)
            {
                // The offset exists
                double lnReturn = laArray[lnOffset];
                laArray[lnOffset] = tnValue;
                return lnReturn;
            }
            else
            {
                // The offset does not exist
                add(tnIndex, tnValue);
                return 0;
            }
        }

        /**
         * Appends the values to the end of the array.
         * @param taValues the values to append
         * @return true if the array has changed as a result of this call
         */
        public synchronized boolean add(double[] taValues)
        {
            double[] laArray = getArray();
            if (laArray.length - m_nEndIndex <= taValues.length)
            {
                // Need to resize the array
                m_aData[m_nDataIndex] = java.util.Arrays.copyOf(laArray, (int)Math.ceil((laArray.length + taValues.length) / m_nFillFactor));
                laArray = getArray();
                // TODO: When the array is too large (larger than max int) need to use multiple arrays
                m_nCapacity = laArray.length;
            }
            java.lang.System.arraycopy(taValues, 0, laArray, m_nEndIndex, taValues.length);
            m_nEndIndex+=taValues.length;
            return true;
        }

    }


    private int m_nCapacity;
    private float m_nFillFactor;
    private boolean m_lHorizontal;
    private double[][] m_aData;
    private karyon.collections.List<ArrayMarker> m_oMarkers;


    /**
     * Creates a new empty vector
     */
    public SparseDoubleVector()
    {
        this(10, .75f, true);
    }

    /**
     * Creates a new empty SparseVector with the specified initial capacity and fill factor
     * @param tnCapacity the initial capacity of the Sparse vector
     * @param tnFillFactor the fill factor for the sparse vector
     */
    public SparseDoubleVector(int tnCapacity, float tnFillFactor, boolean tlIsHorizontal)
    {
        if (tnCapacity <= 0)
        {
            throw new InvalidParameterException("tnCapacity", tnCapacity);
        }
        if (tnFillFactor <= 0 || tnFillFactor >= 1)
        {
            throw new InvalidParameterException("tnFillFactor", tnFillFactor);
        }
        m_nCapacity = tnCapacity;
        m_nFillFactor = tnFillFactor;
        m_lHorizontal = tlIsHorizontal;

        m_aData = new double[1][(int)m_nCapacity];
        m_oMarkers = new karyon.collections.List<ArrayMarker>();
        ArrayMarker loMarker = new ArrayMarker(0, 0, 0);
        m_oMarkers.add(loMarker);
    }

    /**
     * Creates a horizontal sparse array populated with the data from
     * taData
     * @param taData the data to create the array from
     */
    public SparseDoubleVector(double[] taData)
    {
        this(taData, true);
    }

    /**
     * Creates a sparse array populated with the specified data
     * @param taData the data to populate the array with
     * @param tlIsHorizontal true if horizontal, false if vertical
     */
    public SparseDoubleVector(double[] taData, boolean tlIsHorizontal)
    {
        this(taData.length == 0 ? 10 : (int)Math.ceil(taData.length / 0.75f), 0.75f, tlIsHorizontal);
        m_aData = new double[1][m_nCapacity];
        m_oMarkers = new karyon.collections.List<ArrayMarker>();
        ArrayMarker loMarker = new ArrayMarker(0, 0, 0);
        m_oMarkers.add(loMarker);
        loMarker.add(taData);
    }

    /**
     * Checks if this vector representation is vertical or horizontal
     * @return true if a horizontal representation, false otherwise
     */
    public boolean isHorizontal()
    {
        return m_lHorizontal;
    }

    /**
     * Gets the capacity, the amount of data that can be contained
     * within the internal storage mechanism without resizing
     * @return the internal capacity
     */
    public long getCapacity()
    {
        return m_nCapacity;
    }

    @Override
    public boolean isEmpty()
    {
        return m_oMarkers == null || m_oMarkers.size() == 0 || m_oMarkers.get(m_oMarkers.size()-1).m_nEndIndex == 0;
    }

    /**
     * Adds a value to the end of the array
     * @param tnDouble the double to add
     * @return true if the value has been added successfully
     */
    public boolean addDouble(double tnDouble)
    {
        // Always add to the last marker
        return m_oMarkers.get(m_oMarkers.size()-1).add(tnDouble);
    }

    /**
     * This has been marked as deprecated to remind the developer that addDouble
     * should be used instead
     * @param tnDouble the double to add to the end of the array
     * @return true if the array changed as a result of this call
     */
    @Override
    @Deprecated
    public boolean add(Double tnDouble)
    {
        return addDouble(tnDouble);
    }

    /**
     * Returns the full size of the array, this is the index of the last item stored +1.
     * To find the number of concrete elements in the array use count
     * @return the full size of the array
     */
    @Override
    public int size()
    {
        return m_oMarkers == null ? 0 : m_oMarkers.get(m_oMarkers.size()-1).m_nEndIndex;
    }

    /**
     * Gets the total number of elements in the array
     * @return the number of concrete elements in the array
     */
    public long count()
    {
        if (m_oMarkers == null)
        {
            return 0;
        }
        ArrayMarker loMarker = m_oMarkers.get(m_oMarkers.size()-1);
        return loMarker.m_nItemIndex + (loMarker.m_nEndIndex - loMarker.m_nStartIndex);
    }

    /**
     * Use addAllDouble instead
     */
    @Override
    @Deprecated
    public boolean addAll(Collection<? extends Double> taValues)
    {
        throw new UnsupportedOperationException("Use addAllDouble instead");
    }

    /**
     * Appends all of the values to the end of the array
     * @param taValues the values to add
     * @return true if the values have been added
     */
    public boolean addAllDouble(double[] taValues)
    {
        return m_oMarkers.get(m_oMarkers.size()-1).add(taValues);
    }





































    // TODO: Everything below here is to be implemented/tested



    /**
     * Gets the double at the specified position.
     * @param tnIndex the double at the position specified
     * @return the value at the index specified, 0 if there is no value at tnIndex
     */
    public double getDouble(long tnIndex)
    {
        return getMarkerWithIndex(tnIndex).get(tnIndex);
    }

    /**
     * This has been marked as deprecated to remind the developer that
     * getDouble should be used instead of using this method call
     * @param tnIndex the index to retrieve from
     * @return the value at the specified index, or 0 if there is no value at the index specified
     */
    @Override
    @Deprecated
    public Double get(int tnIndex)
    {
        return getDouble(tnIndex);
    }


    /**
     * Gets the marker that should contain the index specified
     * @param tnIndex the index of the marker to find
     * @return the marker which contains the information about the index specified
     */
    private ArrayMarker getMarkerWithIndex(long tnIndex)
    {
        int lnCount = 0;
        for (ArrayMarker loMarker : m_oMarkers)
        {
            if (loMarker.m_nStartIndex > tnIndex)
            {
                // return the previous marker
                return m_oMarkers.get(lnCount-1);
            }
            lnCount ++;
        }
        // return the last marker
        return m_oMarkers.get(m_oMarkers.size()-1) ;
    }
















    /**
     * Marked as deprecated to remind the developer to use setDouble instead
     * @param tnIndex the index to update the value at
     * @param tnValue the value to update to
     * @return the old value or zero if there was no previous value
     */
    @Override
    @Deprecated
    public Double set(int tnIndex, Double tnValue)
    {
        return setDouble(tnIndex, tnValue);
    }

    /**
     * Updates the value at tnIndex with tnValue
     * @param tnIndex the index to update
     * @param tnValue the new value
     * @return the old value or 0 if there was no old value
     */
    public double setDouble(long tnIndex, double tnValue)
    {
        return getMarkerWithIndex(tnIndex).set(tnIndex, tnValue);
    }

    /**
     * Marked as deprecated to remind the developer to use addDouble instead
     * @param tnIndex the index to add the value at
     * @param tnValue the value to add
     */
    @Override
    @Deprecated
    public void add(int tnIndex, Double tnValue)
    {
        addDouble(tnIndex, tnValue);
    }

    /**
     * Adds the value at the specified index, this will
     * shift all of the items after tnIndex
     * @param tnIndex the index to insert the value at
     * @param tnValue the value to insert
     */
    public synchronized void addDouble(long tnIndex, double tnValue)
    {
        ArrayMarker loMarker = getMarkerWithIndex(tnIndex);

        if ((loMarker.m_nStartIndex + loMarker.getCapacity() >= tnIndex || loMarker.m_nEndIndex+1 >= tnIndex))
        {
            // The value can be added inside, or appended to the marker
        }
        else
        {
            // A new marker needs to be created
            ArrayMarker loNewMarker = new ArrayMarker(0, loMarker.m_nEndIndex, (int)tnIndex);
            m_oMarkers.add(loNewMarker);
            loNewMarker.add(tnValue);
        }
    }










    @Override
    public boolean contains(Object o)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<Double> iterator()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object[] toArray()
    {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T[] toArray(T[] ts)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public boolean remove(Object o)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean containsAll(Collection<?> objects)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public boolean addAll(int i, Collection<? extends Double> doubles)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean removeAll(Collection<?> objects)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean replaceAll(Collection<?> objects)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean retainAll(Collection<?> objects)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }





    @Override
    public Double remove(int i)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int indexOf(Object o)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ListIterator<Double> listIterator()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ListIterator<Double> listIterator(int i)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Double> subList(int i, int i2)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
