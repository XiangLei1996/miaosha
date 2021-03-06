package com.miaoshaproject.DAO;

import com.miaoshaproject.DO.ItemStockDO;
import org.apache.ibatis.annotations.Param;

public interface ItemStockDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jun 14 09:33:50 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jun 14 09:33:50 CST 2019
     */
    int insert(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jun 14 09:33:50 CST 2019
     */
    int insertSelective(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jun 14 09:33:50 CST 2019
     */
    ItemStockDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jun 14 09:33:50 CST 2019
     */
    //通过itemId获取对应的库存数量
    ItemStockDO selectByItemId(Integer itemId);

    /**
     * 这里的 MyBatis 接口绑定中， 采用@Param传递多参数的方法；MyBatis传递多参数的方法有哪些？
     * @param itemId
     * @param amount
     * @return 操作正常返回值为1，不正常为0，Service层可通过返回值判断
     */
    int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);


    int updateByPrimaryKeySelective(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jun 14 09:33:50 CST 2019
     */
    int updateByPrimaryKey(ItemStockDO record);
}