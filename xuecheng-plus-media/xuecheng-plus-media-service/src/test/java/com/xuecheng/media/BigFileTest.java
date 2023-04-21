package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author 31331
 * @version 1.0
 * @description TODO
 * @date 2023/3/12 11:02
 */

public class BigFileTest {

    @Test
    public void testChunk() throws IOException {

        //源文件
        File sourceFile = new File("D:\\develop\\bigfile_test\\胡桃.png");

        //分块文件存储路径
        File chunkFolderPath = new File("D:\\develop\\bigfile_test\\chunk\\");
        if (!chunkFolderPath.exists()) {
            chunkFolderPath.mkdirs();
        }

        //设置分块大小
        int chunkSize = 1024 * 1024 * 1;

        //设置分块数量
        long chunkNum = (long) Math.ceil((sourceFile.length()*1.0) / chunkSize);

        //使用流对象读取源文件，向分块中写入数据，达到分块大小后停止写入
        //缓冲区
        byte[] bytes = new byte[1024];
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");

        for (long i = 0; i < chunkNum; i++) {
            //创建分片文件
            File file = new File("D:\\develop\\bigfile_test\\chunk\\" + i);
            if (file.exists()) {
                file.delete();
            }
            boolean newFile = file.createNewFile();

            if (newFile) {
                //向分块文件写入数据流对象
                RandomAccessFile raf_write = new RandomAccessFile(file, "rw");

                int len = -1;
                while ((len = raf_read.read(bytes)) != -1){
                    //向文件中写入数据
                    raf_write.write(bytes, 0, len);

                    //达到分块大小后不再写
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }

                raf_write.close();
            }
        }
        raf_read.close();
    }

    //测试合并
    @Test
    public void testMerge() throws IOException {
        {
            //分块文件存储路径
            File chunkFolderPath = new File("D:\\develop\\bigfile_test\\chunk\\");

            //合并后的文件
            File mergeFile = new File("D:\\develop\\bigfile_test\\merge\\胡桃.png");
            mergeFile.createNewFile();

            if (!chunkFolderPath.exists()) {
                chunkFolderPath.mkdirs();
            }

            //使用流对象读取分块文件，按顺序依次向合并文件写入数据
            File[] chunkFiles = chunkFolderPath.listFiles();
            List<File> chunkFilesList = Arrays.asList(Objects.requireNonNull(chunkFiles));
            //按文件名升序排列
            chunkFilesList.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));

            //创建合并对象的流对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");

            //缓冲区
            byte[] bytes = new byte[1024];
            for (File file : chunkFilesList) {
                //读取分块文件的流对象
                RandomAccessFile raf_read = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = raf_read.read(bytes)) != -1) {
                    //向合并文件写入数据
                    raf_write.write(bytes, 0, len);

                }

            }

            //校验合并后的文件是否正确
            File sourceFile = new File("D:\\develop\\bigfile_test\\胡桃.png");

            FileInputStream sourceInputStream = new FileInputStream(sourceFile);
            FileInputStream mergeInputStream = new FileInputStream(mergeFile);

            if (DigestUtils.md5Hex(sourceInputStream).equals(DigestUtils.md5Hex(mergeInputStream))) {
                System.out.println("合并成功");
            }
        }
    }

}
