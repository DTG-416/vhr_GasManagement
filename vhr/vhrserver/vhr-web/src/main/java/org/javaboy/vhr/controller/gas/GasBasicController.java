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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/gas/basic")
public class GasBasicController {

    @Autowired
    GasService gasService;

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
                                          @RequestParam(required = false)Date[] dateScope) {
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


            int kkkmm=0;
            GasDateInfo gasdateinfo = new GasDateInfo();
            gasdateinfo.setFilePath(uploadDir.getPath());
            gasdateinfo.setOriginalName(baseName);
            gasdateinfo.setUniqueName(uniqueFilename);
            gasdateinfo.setFileExtension(extension);
            gasdateinfo.setUploaderId("00000001");

            int kk=0;

            //先写数据库再保存文件
            if(0!=gasService.addGasDataInfo(gasdateinfo)){
                File destination = new File(uploadDir, uniqueFilename);  // 使用 File.separator
                file.transferTo(destination);
            }
            int kkk=0;
            return ResponseEntity.ok("文件上传成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("上传失败: " + e.getMessage());
        }
    }
    /*
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> FileUpload(@RequestPart("files") List<MultipartFile> files) {
        // 用于存储返回的结果
        Map<String, Object> response = new HashMap<>();
        List<String> successList = new ArrayList<>();  // 存储成功上传的文件信息
        List<String> failedList = new ArrayList<>();   // 存储上传失败的文件信息

        try {
            // 获取当前运行目录（用于存储上传文件）
            String currentDir = System.getProperty("user.dir");
            String fileDir = "/uploads";
            // 定义并创建上传目录
            File uploadDir = new File(currentDir + fileDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 如果目录不存在，则创建
            }

            // 遍历每个上传的文件
            for (MultipartFile file : files) {
                try {
                    // 获取文件的原始名称
                    String originalFilename = file.getOriginalFilename();

                    // 检查文件名是否有效
                    if (originalFilename == null || originalFilename.isEmpty()) {
                        failedList.add("文件名不能为空");
                        continue; // 跳过该文件
                    }

                    // 提取文件的基本名称和扩展名
                    String baseName;
                    String extension = "";
                    int lastDotIndex = originalFilename.lastIndexOf(".");

                    if (lastDotIndex != -1 && lastDotIndex != originalFilename.length() - 1) {
                        baseName = originalFilename.substring(0, lastDotIndex); // 获取文件名（不包括扩展名）
                        extension = originalFilename.substring(lastDotIndex + 1); // 获取扩展名
                    } else {
                        baseName = originalFilename; // 没有扩展名的情况
                    }

                    // 生成唯一的文件名（使用UUID避免文件名冲突）
                    String uniqueFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

                    // 构造文件信息对象，并存入数据库
                    GasDateInfo gasdateinfo = new GasDateInfo();
                    gasdateinfo.setFilePath(uploadDir.getPath());  // 存储路径
                    gasdateinfo.setOriginalName(baseName); // 原始文件名（不含扩展名）
                    gasdateinfo.setUniqueName(uniqueFilename); // 唯一文件名
                    gasdateinfo.setFileExtension(extension); // 文件扩展名
                    gasdateinfo.setUploaderId("00000001"); // 假设的上传者ID

                    // 先存数据库，保证文件记录能查询到
                    if (gasService.addGasDataInfo(gasdateinfo) != 0) {
                        // 构造文件存储路径
                        File destination = new File(uploadDir, uniqueFilename);
                        // 保存文件到服务器目录
                        file.transferTo(destination);
                        successList.add(originalFilename + " 上传成功");
                    } else {
                        failedList.add(originalFilename + " 数据库保存失败");
                    }
                } catch (Exception e) {
                    failedList.add("文件处理失败: " + e.getMessage());
                }
            }

            // 组装返回结果
            response.put("success", successList);
            response.put("failed", failedList);

            // 判断是否有失败的文件
            if (failedList.isEmpty()) {
                return ResponseEntity.ok(response); // 全部成功，返回 200 OK
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response); // 部分成功，返回 206 Partial Content
            }
        } catch (Exception e) {
            // 发生异常，返回 500 错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "上传失败: " + e.getMessage()));
        }
    }
       */
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

    //多个文件下载
    @RequestMapping("/batchDownload")
    public ResponseEntity<StreamingResponseBody> batchDownload(@RequestBody List<GasDateInfo> fileRequests) {
        AtomicInteger index= new AtomicInteger();
        StreamingResponseBody responseBody = outputStream -> {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(outputStream));
            try {
                for (GasDateInfo fileRequest : fileRequests) {
                    Path filePath = Paths.get(fileRequest.getFilePath(),fileRequest.getUniqueName());

                    if (!Files.exists(filePath)) {
                        System.err.println("File not found: " + filePath);
                        continue;
                    }

                    ZipEntry entry = new ZipEntry(index.getAndIncrement() + "_" + fileRequest.getOriginalName() + "." + fileRequest.getFileExtension());
                    zos.putNextEntry(entry);
                    try (InputStream fis = Files.newInputStream(filePath)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }
                    zos.closeEntry();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        return ResponseEntity.ok()
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=batch_download.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }


    // 计算CRC32并避免重复读取文件
    private long computeCrc32(Path filePath) throws IOException {
        CRC32 crc = new CRC32();
        try (InputStream fis = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }
        return crc.getValue();
    }
}

