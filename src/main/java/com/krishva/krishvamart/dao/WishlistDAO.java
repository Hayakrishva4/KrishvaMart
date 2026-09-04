package com.krishva.krishvamart.dao;

import com.krishva.krishvamart.exception.DataAccessException;
import com.krishva.krishvamart.model.WishlistItem;
import java.util.List;

public interface WishlistDAO {

    WishlistItem add(long userId, long productId) throws DataAccessException;
    boolean remove(long userId, long productId) throws DataAccessException;
    List<WishlistItem> findByUser(long userId) throws DataAccessException;
    boolean exists(long userId, long productId) throws DataAccessException;
}
