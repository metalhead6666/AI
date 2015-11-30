/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jclal.core;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Interface that represents a dataset.
 *
 * @author Oscar Gabriel Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public interface IDataset extends JCLAL {

    /**
     * @return The dataset in Weka format
     */
    public Instances getDataset();

    /**
     * @return The number of attributes of the dataset
     */
    public int getNumAttributes();

    /**
     * @return A cpoy of the current dataset
     */
    public IDataset copy();

    /**
     * Add to the current dataset the dataset passed as argument
     *
     * @param dataset The dataset to addAll
     */
    public void addAll(IDataset dataset);

    /**
     * @return The number of instances of the dataset
     */
    public int getNumInstances();

    /**
     * @param index The index of the instance
     * @return The instance
     */
    public Instance instance(int index);

    /**
     * Set the instance in the specified position
     *
     * @param index The index of the position
     * @param instance The instance to set in the position
     */
    public void set(int index, Instance instance);

    /**
     *
     * @return True if the dataset is empty, false otherwise.
     */
    public boolean isEmpty();

    /**
     * Add an instance to the dataset
     *
     * @param instance The instance to add
     */
    public void add(Instance instance);

    /**
     * Remove an instance from dataset
     *
     * @param index The index of the instance to remove
     */
    public void remove(int index);

    /**
     * Removes all elements
     */
    public void delete();

}