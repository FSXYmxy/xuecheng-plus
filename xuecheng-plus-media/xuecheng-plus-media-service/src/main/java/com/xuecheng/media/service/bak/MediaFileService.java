//package com.xuecheng.media.service;
//
//import com.xuecheng.base.model.PageParams;
//import com.xuecheng.base.model.PageResult;
//import com.xuecheng.base.model.RestResponse;
//import com.xuecheng.media.model.dto.QueryMediaParamsDto;
//import com.xuecheng.media.model.dto.UploadFileParamsDto;
//import com.xuecheng.media.model.dto.UploadFileResultDto;
//import com.xuecheng.media.model.po.MediaFiles;
//
//import java.io.File;
//
///**
// * @author 31331
// * @version 1.0
// * @description媒资管理服务
// * @date 2023/3/10 10:23
// */
//
//public interface MediaFileService {
//
//    /**
//    * @description 上传文件的通用接口
//    * @param companyId 机构id
//     * @param uploadFileParamsDto 文件信息
//     * @param bytes 文件字节数据
//     * @param folder 桶（bucket）下面的子目录
//     * @param fileName 文件名
//    * @return com.xuecheng.media.model.dto.UploadFileResultDto
//    * @author 31331
//    * @date 2023/3/10 10:33
//    */
//    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String fileName);
//
//    /**
//    * @description 文件查询接口
//    * @param companyId 机构id
//     * @param pageParams 分页参数
//     * @param queryMediaParamsDto 文件参数，包含名称，类型和审核状态
//    * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
//    * @author 31331
//    * @date 2023/3/12 10:00
//    */
//    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);
//
//
//    /**
//    * @description 校验文件
//    * @param fileMd5 文件的md5值
//    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
//    * @author 31331
//    * @date 2023/3/13 10:27
//    */
//    RestResponse<Boolean> checkFile(String fileMd5);
//
//    /**
//    * @description 校验文件分片
//    * @param fileMd5 文件的md5值
//     * @param chunkIndex 分片的下标
//    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
//    * @author 31331
//    * @date 2023/3/13 10:28
//    */
//    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);
//
//    /**
//    * @description 上传分块
//    * @param fileMd5 文件的md5值
//     * @param chunk 分片的md5值
//     * @param bytes 文件字节
//    * @return com.xuecheng.base.model.RestResponse
//    * @author 31331
//    * @date 2023/3/13 11:21
//    */
//    RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes);
//
//    /**
//    * @description 合并分块
//    * @param companyId 机构id
//     * @param fileMd5 文件md5值
//     * @param chunkTotal 分片总数
//     * @param uploadFileParamsDto 上传文件的参数
//    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
//    * @author 31331
//    * @date 2023/3/17 10:03
//    */
//    RestResponse<Boolean> mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);
//
//
//    /***
//    * @description 根据id查文件
//    * @param id
//    * @return com.xuecheng.media.model.po.MediaFiles
//    * @author 31331
//    * @date 2023/3/17 10:04
//    */
//    MediaFiles getFileById(String id);
//
//    /***
//    * @description 从minio下载文件到本地
//     * @param bucket
//     * @param objectName
//    * @return java.io.File
//    * @author 31331
//    * @date 2023/3/21 13:36
//    */
//    File downloadFileFromMinIO(String bucket, String objectName);
//
//    /***
//    * @description 添加文件到minio
//    * @param filePath
//     * @param mineType
//     * @param bucket
//     * @param objectName
//    * @return boolean
//    * @author 31331
//    * @date 2023/3/21 14:27
//    */
//    boolean addMediaFileToMinio(String filePath, String mineType, String bucket, String objectName);
//
//}
