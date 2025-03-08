package org.javaboy.vhr.controller.gas;

import org.javaboy.vhr.model.GasDateInfo;
import org.javaboy.vhr.model.RespPageBean;
import org.javaboy.vhr.service.GasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/gas/basic")
public class GasBasicController {

    @Autowired
    public GasService gasService;

    //查询页面数据
    //@RequestParam(defaultValue = "1") Integer page：
    //接收 page 参数，默认值 1（第一页）。
    //变量名前后一致，内容自动映射
    @GetMapping("/")
    public RespPageBean getGasDatesByPage(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String originalname,
                                          @RequestParam(required = false)Integer beginnum,
                                          @RequestParam(required = false)Integer endnum,
                                          @RequestParam(required = false) Date[] dateScope) {
        return gasService.getGasDatesByPage(page, size, originalname,beginnum,endnum,dateScope);
    }


    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestPart("file") MultipartFile file) {
        try {
            //获取当前运行目录
            String currentDir = System.getProperty("user.dir");

            //创建文件路径
            File uploadDir = new File(currentDir + File.separator + "uploads");

            //获得原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("文件名不能为空");
            }
            //分离文件名与文件类型
            // 分离文件名与文件类型
            String baseName;
            String extension = "";
            //查找最后的'.'
            int lastDotIndex = originalFilename.lastIndexOf(".");
            //存在'.'并且'.'后面有字符
            if (lastDotIndex != -1 && lastDotIndex != originalFilename.length() - 1) { // 确保 "." 不是最后一个字符
                //文件名
                baseName = originalFilename.substring(0, lastDotIndex);
                //类型名
                extension = originalFilename.substring(lastDotIndex + 1);
            } else {
                //文件名
                baseName = originalFilename;
            }

            // 生成唯一文件名（UUID）
            String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            // 检查目录是否存在，不存在则创建
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();  // 递归创建多级目录
            }


            GasDateInfo gasdateinfo = new GasDateInfo();
            gasdateinfo.setFilePath(uploadDir.getPath());
            gasdateinfo.setOriginalName(baseName);
            gasdateinfo.setUniqueName(uniqueFilename);
            gasdateinfo.setFileExtension(extension);
            gasdateinfo.setUploaderId("00000001");


            //先写数据库再保存文件
            if(0!=gasService.addGasDataInfo(gasdateinfo)){
                File destination = new File(uploadDir, uniqueFilename);  // 使用 File.separator
                file.transferTo(destination);
            }
            return ResponseEntity.ok("文件上传成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }

    //单个文件下载
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam String uniqueName,
            @RequestParam String fileExtension,
            @RequestParam String originalName,
            @RequestParam String filePath) {
        try {
            //文件完整路径
            Path FullPath = Paths.get(filePath+"\\" + uniqueName);

            //判断文件是否存在
            if (!Files.exists(FullPath)) {
                return ResponseEntity.notFound().build();
            }

            //创建资源保存文件
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(FullPath));
            //资源实际名称
            String downloadFileName = originalName + "." + fileExtension;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 多个文件下载
    @RequestMapping("/batchDownload")
    public ResponseEntity<StreamingResponseBody> batchDownload(@RequestBody List<GasDateInfo> fileRequests) {
        StreamingResponseBody responseBody = outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
                int index = 0;
                for (GasDateInfo fileRequest : fileRequests) {
                    Path filePath = Paths.get(fileRequest.getFilePath(), fileRequest.getUniqueName());

                    if (!Files.exists(filePath)) {
                        //logger.error("文件未找到: {}", filePath);
                        continue;
                    }

                    ZipEntry entry = new ZipEntry(index + "_" + fileRequest.getOriginalName() + "." + fileRequest.getFileExtension());
                    zos.putNextEntry(entry);
                    try (InputStream fis = Files.newInputStream(filePath)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        //logger.error("读取文件时发生错误: {}", filePath, e);
                    } finally {
                        zos.closeEntry();
                    }
                    index++;
                }
            } catch (IOException e) {
                //logger.error("创建ZIP文件时发生错误", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=batch_download.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }


}

