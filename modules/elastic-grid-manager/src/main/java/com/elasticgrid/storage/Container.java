/**
 * Elastic Grid
 * Copyright (C) 2008-2010 Elastic Grid, LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.elasticgrid.storage;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * A container is a set of objects stored in the cloud.
 * @author Jerome Bernard
 */
public interface Container extends Serializable {
    /**
     * Return the name of the container.
     * @return name of the container
     */
    String getName();

    /**
     * List the {@link Storable}s that are available in the container.
     * @return the {@link Storable}s which are in this container
     * @throws StorageException if the list of storables can't be retrieved
     */
    List<Storable> listStorables() throws StorageException;

    /**
     * Retrieve a {@link Storable} by name (also called key).
     * @param name the name of the storable
     * @return the {@link Storable} found
     * @throws StorableNotFoundException if there is no storable having that name
     * @throws StorageException if the storable can't be retrieved
     */
    Storable findStorableByName(String name) throws StorableNotFoundException, StorageException;

    /**
     * Upload a {@link Storable} from a {@link File}.
     * This method is the same as calling {@link #uploadStorable(String,File)} where the key is the filename.
     * @param file the file to upload as a {@link Storable}
     * @throws StorageException if the upload can't be done
     * @return the created storable
     * @see #uploadStorable(String, File)
     */
    Storable uploadStorable(File file) throws StorageException;

    /**
     * Upload a {@link Storable} from a {@link File}.
     * @param name the {@link Storable} key
     * @param file the file to upload as a {@link Storable}
     * @return the created storable
     * @throws StorageException if the upload can't be done
     */
    Storable uploadStorable(String name, File file) throws StorageException;

    /**
     * Upload a {@link Storable} from an {@link InputStream}.
     * @param name the {@link Storable} key
     * @param stream the input stream to upload as a {@link Storable}
     * @param mimeType the MIME-type of the content to upload
     * @return the created storable
     * @throws StorageException if the upload can't be done
     */
    Storable uploadStorable(String name, InputStream stream, String mimeType) throws StorageException;

    /**
     * Delete a {@link Storable} by name.
     * @param name the name of the storable to delete
     * @throws StorageException if the deletion can't be done
     */
    void deleteStorable(String name) throws StorageException;
}
