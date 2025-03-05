package org.javaboy.vhr.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class GasDateInfo implements Serializable {
    private Long id;              // 文件记录的唯一ID（可选）
    private String uniqueName;    // 文件的唯一名称（UUID）
    private String originalName;  // 文件的真实名称（用户上传时的名称）

    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    private Date uploadDate; // 文件上传日期

    private String filePath;      // 文件保存路径
    private String fileExtension; // 文件后缀（类型）
    private String uploaderId;      // 上传者ID

    // 构造方法
    public GasDateInfo() {this.uploadDate =new java.sql.Date(System.currentTimeMillis()); }

    public GasDateInfo(String uniqueName, String originalName, String filePath, String fileExtension, String uploaderId) {
        this.uniqueName = uniqueName;
        this.originalName = originalName;
        this.uploadDate =new java.sql.Date(System.currentTimeMillis()); // 默认当前时间
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.uploaderId = uploaderId;
    }

    // Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUniqueName() { return uniqueName; }
    public void setUniqueName(String uniqueName) { this.uniqueName = uniqueName; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public Date getUploadDate() { return uploadDate; }
    public void setUploadDate(Date date) { this.uploadDate = date; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", uniqueName='" + uniqueName + '\'' +
                ", originalName='" + originalName + '\'' +
                ", uploadDate=" + uploadDate +
                ", filePath='" + filePath + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", uploaderId=" + uploaderId +
                '}';
    }
}
