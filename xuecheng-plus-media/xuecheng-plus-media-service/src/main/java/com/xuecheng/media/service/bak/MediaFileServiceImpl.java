//package com.xuecheng.media.service.bak;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.j256.simplemagic.ContentInfo;
//import com.j256.simplemagic.ContentInfoUtil;
//import com.xuecheng.base.exception.XueChengPlusException;
//import com.xuecheng.base.model.PageParams;
//import com.xuecheng.base.model.PageResult;
//import com.xuecheng.base.model.RestResponse;
//import com.xuecheng.media.mapper.MediaFilesMapper;
//import com.xuecheng.media.mapper.MediaProcessMapper;
//import com.xuecheng.media.model.dto.QueryMediaParamsDto;
//import com.xuecheng.media.model.dto.UploadFileParamsDto;
//import com.xuecheng.media.model.dto.UploadFileResultDto;
//import com.xuecheng.media.model.po.MediaFiles;
//import com.xuecheng.media.model.po.MediaProcess;
//import com.xuecheng.media.service.MediaFileService;
//import io.minio.GetObjectArgs;
//import io.minio.MinioClient;
//import io.minio.PutObjectArgs;
//import io.minio.UploadObjectArgs;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.util.*;
//
//
//@Slf4j
//@Service
//public class MediaFileServiceImpl implements MediaFileService {
//
//    @Autowired
//    MediaFilesMapper mediaFilesMapper;
//    @Autowired
//    MinioClient minioClient;//在MinioConfig中设置了了初始化后的对象
//    @Autowired
//    MediaProcessMapper mediaProcessMapper;
//    @Value("${minio.bucket.files}")
//    private String bucket_files;
//    @Value("${minio.bucket.videofiles}")
//    private String bucket_videofiles;
//
//    @Override
//    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
//
//        String fileMd5 = DigestUtils.md5Hex(bytes);
//
//        if (StringUtils.isEmpty(folder)) {
//            //当目录为空时，按照年月日自动生成目录的路径
//            folder = getFileFolder(new Date(), true, true, true);
//        } else if (!folder.contains("/")) {
//            //目录没加斜杠就加上
//            folder = folder + "/";
//        }
//
//        String filename = uploadFileParamsDto.getFilename();
//
//        if (StringUtils.isEmpty(objectName)) {
//            //当文件名为空时，自动生成文件名
//            objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
//
//        }
//        //得到文件名
//        objectName = folder + objectName;
//        MediaFiles mediaFiles = null;
//
//        try {
//            //上传到minIo
//            addMediaFileToMinio(bytes, bucket_files, objectName);
//
//            //上传到数据库
//            mediaFiles = addMediaFileToDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);
//        } catch (Exception e) {
//            XueChengPlusException.cast("插入数据出错，请重试" + e.getMessage());
//        }
//        //准备返回数据
//        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
//        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
//
//
//        return uploadFileResultDto;
//
//    }
//
//    @Override
//    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
//
//        //初始化查询器
//        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
//
//        String filename = queryMediaParamsDto.getFilename();
//        String fileType = queryMediaParamsDto.getFileType();
//
//        //必须判断一下，不然接口不可用
//        if (filename != null && !filename.equals("")) {
//            //模糊查询名字
//            queryWrapper
//                    .like(MediaFiles::getFilename, filename);
//        }
//
//        if (fileType != null && !fileType.equals("")){
//            //文件类型
//            queryWrapper
//                    .eq(MediaFiles::getFileType, fileType);
//        }
//
//
//        Page<MediaFiles> mediaFilesPage = mediaFilesMapper.selectPage(new Page<>(pageParams.getPageNo(), pageParams.getPageSize()), queryWrapper);
//
//        return new PageResult<>(mediaFilesPage.getRecords(), mediaFilesPage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
//    }
//
//    @Override
//    public RestResponse<Boolean> checkFile(String fileMD5) {
//
//        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMD5);
//
//        //检查文件是否在数据库中有记录
////        if (mediaFiles == null) {
////            return RestResponse.success(false);
////        }
//
//        //检查文件是否在文件系统中存在
//        try {
//            InputStream stream = minioClient.getObject(
//                    GetObjectArgs.builder()
//                            .bucket(bucket_videofiles)
//                            .object(mediaFiles.getFilePath())
//                            .build()
//            );
//
//            if (stream == null) {
//                return RestResponse.success(false);
//            }
//        } catch (Exception e) {
//            return RestResponse.success(false);
//        }
//
//        //文件存在，返回true
//        return RestResponse.success(true);
//    }
//
//    @Override
//    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
//        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
//
//        String chunkFilePath = chunkFileFolderPath + chunkIndex;
//
//        //检查文件是否在文件系统中存在
//        try {
//            InputStream stream = minioClient.getObject(
//                    GetObjectArgs.builder()
//                            .bucket(bucket_videofiles)
//                            .object(chunkFilePath)
//                            .build()
//            );
//
//            if (stream == null) {
//                return RestResponse.success(false);
//            }
//        } catch (Exception e) {
//            return RestResponse.success(false);
//        }
//
//        //文件存在，返回true
//        return RestResponse.success(true);
//    }
//
//    @Override
//    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, byte[] bytes) {
//
//        //得到分块文件的目录
//        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
//        //的分块文件的路径
//        String chunkFile = chunkFileFolderPath + chunk;
//
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
//
//        try {
//            //上传文件
//            minioClient.putObject(
//                    PutObjectArgs
//                            .builder()
//                            .bucket(bucket_videofiles)
//                            .stream(inputStream, inputStream.available(), -1)
//                            .object(chunkFile)
//                    .build());
//
//            return RestResponse.success(true);
//
//        } catch (Exception e) {
//            return RestResponse.success(false);
//        }
//    }
//
//    @Override
//    public RestResponse<Boolean> mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
//
//        //下载分块
//        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);
//
//        //获取合并后的文件拓展名
//        String filename = uploadFileParamsDto.getFilename();
//        String extension = filename.substring(filename.lastIndexOf("."));
//
//        //创建临时合并文件
//        File tempMergeFile = null;
//        try {
//            try {
//                tempMergeFile = File.createTempFile("mergeFile", extension);
//            } catch (IOException e) {
//                XueChengPlusException.cast("创建临时合并文件出错：" + e.getMessage());
//            }
//
//            RandomAccessFile raf_write = null;
//
//            try {
//                //创建合并对象的流对象
//                raf_write = new RandomAccessFile(tempMergeFile, "rw");
//
//                //缓冲区
//                byte[] bytes = new byte[1024];
//                for (File file : chunkFiles) {
//                    //读取分块文件的流对象
//                    RandomAccessFile raf_read = null;
//
//                    try {
//                        raf_read = new RandomAccessFile(file, "r");
//                        int len = -1;
//                        while ((len = raf_read.read(bytes)) != -1) {
//                            //向合并文件写入数据
//                            raf_write.write(bytes, 0, len);
//
//                        }
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    } finally {
//                        if (raf_read != null) {
//                            raf_read.close();
//                        }
//                    }
//
//                }
//
//                //校验合并后的文件是否正确
//                FileInputStream mergeInputStream = new FileInputStream(tempMergeFile);
//
//                if (!fileMd5.equals(DigestUtils.md5Hex(mergeInputStream))){
//                    log.error("合并文件校验不通过{},原始文件md5值{}", tempMergeFile.getAbsolutePath(), fileMd5);
//                    XueChengPlusException.cast("合并文件校验失败");
//                }
//            }catch (Exception e){
//                XueChengPlusException.cast("合并文件时出错：" + e.getMessage());
//            } finally {
//                try {
//                    if (raf_write != null) {
//                        raf_write.close();
//                    }
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//            String filePath = getChunkFileFolderPath(fileMd5, extension);
//
//            //将合并后的文件上传到文件系统
//            addMediaFileToMinio(tempMergeFile.getAbsolutePath(), getMineTypeByExtension(extension), bucket_videofiles, filePath);
//
//            //将文件信息入库保存
//            uploadFileParamsDto.setFileSize(tempMergeFile.length());
//            addMediaFileToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, filePath);
//        } finally {
//            //删除临时分块文件
//            if (chunkFiles != null) {
//                for (File chunkFile : chunkFiles) {
//                    if (chunkFile.exists()) {
//                        chunkFile.delete();
//                    }
//                }
//            }
//
//            //删除合并后的文件
//            if (tempMergeFile != null) {
//                tempMergeFile.delete();
//            }
//
//
//
//        }
//
//        return RestResponse.success(true);
//    }
//
//    @Override
//    public MediaFiles getFileById(String id) {
//
//        MediaFiles mediaFiles = mediaFilesMapper.selectById(id);
//
//        if (mediaFiles == null) {
//            XueChengPlusException.cast("文件不存在");
//        }
//
//        if (mediaFiles.getUrl() == null) {
//            XueChengPlusException.cast("文件尚未处理，请稍后预览");
//        }
//
//        return mediaFiles;
//    }
//
//    /**
//    * @description 下载分块
//    * @param fileMd5
//     * @param chunkTotal
//    * @return java.io.File[] 文件数组
//    * @author 31331
//    * @date 2023/3/15 10:20
//    */
//    private File[] checkChunkStatus(String fileMd5, int chunkTotal){
//
//        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
//
//        File[] chunkFiles = new File[chunkTotal];
//
//        for (int i = 0; i < chunkTotal; i++) {
//            String chunkFilePath = chunkFileFolderPath + i;
//            File chunkFile = null;
//            try {
//                chunkFile = File.createTempFile("chunk", null);
//            } catch (IOException e) {
//                XueChengPlusException.cast("创建分块临时文件出错："+ e.getMessage());
//            }
//
//            //下载分块文件
//            File file = downloadFileFromMinIO(bucket_videofiles, chunkFilePath);
//            chunkFiles[i] = file;
//        }
//
//        return chunkFiles;
//    }
//
//    //根据桶和文件名从minio下载文件
//    public File downloadFileFromMinIO(String bucket, String objectName){
//        try {
//            //获取文件流
//            InputStream inputStream = minioClient.getObject(
//                    GetObjectArgs
//                            .builder()
//                            .bucket(bucket)
//                            .object(objectName)
//                            .build());
//
//
//            File tempFile = File.createTempFile("minio", ".merge");
//
//            FileOutputStream outputStream = new FileOutputStream(tempFile);
//
//
//            //装填临时文件
//            IOUtils.copy(inputStream, outputStream);
//            //填充数组
//            return tempFile;
//
//        } catch (Exception e) {
//            XueChengPlusException.cast("下载分块时出现异常：" + e.getMessage());
//        }
//
//        return  null;
//    }
//
//    //得到分块文件的目录
//    public String getChunkFileFolderPath(String fileMd5) {
//        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
//    }
//
//    //得到文件目录
//    public String getChunkFileFolderPath(String fileMd5, String extension) {
//        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + extension;
//    }
//
//    //根据拓展名拿取匹配的文件类型
//    public String getMineTypeByExtension(String extension){
//        //资源的媒体类型
//        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认为未知二进制字节流
//
//        if (StringUtils.isNotEmpty(extension)) {
//            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
//            if (extensionMatch != null) {
//                contentType = extensionMatch.getMimeType();
//            }
//        }
//
//        return contentType;
//    }
//
//
//
//
//    //根据日期拼接目录
//    public String getFileFolder(Date date, boolean year, boolean month, boolean day) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        //获取当前日期字符串
//        String dateString = sdf.format(new Date());
//        //取出年、月、日
//        String[] dateStringArray = dateString.split("-");
//        StringBuffer folderString = new StringBuffer();
//        if (year) {
//            folderString.append(dateStringArray[0]);
//            folderString.append("/");
//        }
//        if (month) {
//            folderString.append(dateStringArray[1]);
//            folderString.append("/");
//        }
//        if (day) {
//            folderString.append(dateStringArray[2]);
//            folderString.append("/");
//        }
//        return folderString.toString();
//    }
//
//    //标记
//    public boolean addMediaFileToMinio(String filePath, String mineType, String bucket, String objectName) {
//        try {
//
//            //上传到minio
//            minioClient.uploadObject(
//                    UploadObjectArgs
//                            .builder()
//                            .bucket(bucket)
//                            .object(objectName)
//                            .filename(filePath)
//                            .contentType(mineType)
//                            .build()
//            );
//        } catch (Exception e) {
//            log.debug("上传文件失败{}" + e.getMessage());
//            XueChengPlusException.cast("上传文件到系统出错");
//            return false;
//        }
//
//        return true;
//    }
//
//    //通用的上传文件到minIo的方法
//    private void addMediaFileToMinio(byte[] bytes, String bucket, String objectName) {
//
//        //资源的媒体类型
//        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认为未知二进制字节流
//
//        if (objectName.contains(".")) {
//            String extension = objectName.substring(objectName.lastIndexOf("."));
//            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
//
//            if (extensionMatch != null) {
//                contentType = extensionMatch.getMimeType();
//            }
//
//        }
//
//        try {
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//
//            //上传到minio
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(bucket)
//                            .object(objectName)
//                            .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
//                            .contentType(contentType)
//                            .build()
//            );
//        } catch (Exception e) {
//            log.debug("上传文件失败{}" + e.getMessage());
//            XueChengPlusException.cast("上传文件到系统出错");
//        }
//
//    }
//
//    @Transactional
//    //上传文件到数据库
//    public MediaFiles addMediaFileToDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
//
//        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
//
//        if (mediaFiles == null) {
//            mediaFiles = new MediaFiles();
//
//            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
//
//            mediaFiles.setId(fileId);
//            mediaFiles.setFileId(fileId);
//            mediaFiles.setCompanyId(companyId);
//            mediaFiles.setBucket(bucket);
//            mediaFiles.setFilename(uploadFileParamsDto.getFilename());
//            mediaFiles.setFilePath(objectName);
//
//            //获取拓展名
//            String extension = null;
//
//            String filename = uploadFileParamsDto.getFilename();
//            if (StringUtils.isNotEmpty(filename) && filename.contains(".")) {
//                extension = filename.substring(filename.lastIndexOf("."));
//            }
//
//            //媒体类型
//            String mineType = getMineTypeByExtension(extension);
//
//            //图片，mp4视频可以设置url
//            if (mineType.contains("png") || mineType.contains("jpg") || mineType.contains("mp4")) {
//                mediaFiles.setUrl("/" + bucket + "/" + objectName);
//            }
//
//            mediaFiles.setCreateDate(LocalDateTime.now());
//            mediaFiles.setStatus("1");
//            mediaFiles.setAuditStatus("002003");
//
//            //插入文件表
//            mediaFilesMapper.insert(mediaFiles);
//
//            //记录待处理任务
//            addWaitingTasks(mediaFiles);
//
//        }
//        return mediaFiles;
//    }
//
//
//    /**
//    * @description 添加待处理任务
//    * @param mediaFiles 媒资信息
//    * @author 31331
//    * @date 2023/3/20 17:53
//    */
//    private void addWaitingTasks(MediaFiles mediaFiles){
//        //通过mineType判断如果是avi视频则写入待处理任务
//        String filename = mediaFiles.getFilename();
//
//        String mineType = getMineTypeByExtension(filename.substring(filename.lastIndexOf(".")));
//
//        if (mineType.equals("video/x-msvideo")) {
//            MediaProcess mediaProcess = new MediaProcess();
//
//            BeanUtils.copyProperties(mediaFiles, mediaProcess);
//
//            //状态
//            mediaProcess.setStatus("1");
//            mediaProcess.setCreateDate(LocalDateTime.now());
////            mediaProcess.setFailCount(0);
//            mediaProcess.setUrl(null);
//
//            //向mediaProcess插入记录
//            mediaProcessMapper.insert(mediaProcess);
//        }
//    }
//
//
//
//}
//
