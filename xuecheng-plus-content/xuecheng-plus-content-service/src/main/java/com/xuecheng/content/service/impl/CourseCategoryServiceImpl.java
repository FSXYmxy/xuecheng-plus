package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/2/13 0:01
 */

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //得到节点下的所有子节点
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //定义一个list作为最终返回的数据
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();

        //为了方便找子节点，定义一个map
        HashMap<String, CourseCategoryTreeDto> nodeMap = new HashMap<>();

        //将数据封装到list中，只包括了根节点的直接下属节点
        categoryTreeDtos.stream().forEach(item->{
            nodeMap.put(item.getId(), item);
            if (item.getParentid() .equals(id)){
                courseCategoryTreeDtos.add(item);
            }

            //找到该节点的父节点
            String parentid = item.getParentid();
            //得到父节点对象
            CourseCategoryTreeDto parentNode = nodeMap.get(parentid);

            if (parentNode != null){
                List childrenTreeNodes = parentNode.getChildrenTreeNodes();
                //父节点的ChildrenTreeNodes为空就初始化一下
                if (childrenTreeNodes == null){
                    parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }

                //找到子节点，放到他的父节点的ChildrenTreeNodes中
                parentNode.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryTreeDtos;
    }
}
