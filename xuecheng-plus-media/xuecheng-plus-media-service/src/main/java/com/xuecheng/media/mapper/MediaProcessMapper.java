package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */

public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} and (t.status = '1' or t.status = '3') limit #{count};")
//    @Select("SELECT * FROM media_process mp WHERE mp.id % #{shardTotal} = #{shardIndex} AND (mp.`status` = 1 OR mp.`status` = 3) limit #{count};")
    List<MediaProcess> selectListByShardIndex(
            @Param("shardTotal") int shardTotal,
            @Param("shardIndex") int shardIndex,
            @Param("count") int count); //没用上


    @Update("update media_process mp set mp.status='4' where (mp.status='1' or mp.status='3') and mp.id = #{id}")
    int startTask(@Param("id") Long id);
}
