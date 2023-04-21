//package com.xuecheng.media.api.bak;
//
//import com.xuecheng.base.exception.XueChengPlusException;
//import com.xuecheng.base.model.PageParams;
//import com.xuecheng.base.model.PageResult;
//import com.xuecheng.base.model.RestResponse;
//import com.xuecheng.media.model.dto.QueryMediaParamsDto;
//import com.xuecheng.media.model.dto.UploadFileParamsDto;
//import com.xuecheng.media.model.dto.UploadFileResultDto;
//import com.xuecheng.media.model.po.MediaFiles;
//import com.xuecheng.media.service.MediaFileService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
///**
// * @author Mr.M
// * @version 1.0
// * @description 媒资文件管理接口
// * @date 2022/9/6 11:29
// */
//@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
//@RestController
//public class MediaFilesController {
//
//
//    @Autowired
//    MediaFileService mediaFileService;
//
//
//    @ApiOperation("媒资列表查询接口")
//    @PostMapping("/files")
//    @ResponseBody
//    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
//        Long companyId = 1232141425L;
//        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);
//
//    }
//
//
//    @ApiOperation("上传文件")
//    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @ResponseBody
//    public UploadFileResultDto upload(
//            @RequestPart("filedata") MultipartFile filedata,
//            @RequestParam(value = "folder", required = false) String folder,
//            @RequestParam(value = "objectName", required = false) String objectName)
//    {
//
//        Long companyId = 1232141425L;
//
//        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
//        //确认文件类型
//        uploadFileParamsDto.setContentType(filedata.getContentType());
//        String contentType = filedata.getContentType();
//        uploadFileParamsDto.setFileSize(filedata.getSize());
//
//        if (contentType.contains("image")) {
//            //是个图片
//            uploadFileParamsDto.setFileType("001001");
//        } else {
//            uploadFileParamsDto.setFileType("001003");
//        }
//
//        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());//文件名称
//
//        UploadFileResultDto uploadFileResultDto = null;
//
//        try {
//            uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamsDto, filedata.getBytes(), folder, objectName);
//        } catch (IOException e) {
//            XueChengPlusException.cast("上传文件的过程中出错");
//        }
//        return uploadFileResultDto;
//    }
//
//
//    @ApiOperation("预览文件")
//    @GetMapping("/preview/{mediaId}")
//    public RestResponse<String > getPlayUrlByMediaId(@PathVariable String mediaId){
//
//        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
//
//        return RestResponse.success(mediaFiles.getUrl());
//    }
//
//}
