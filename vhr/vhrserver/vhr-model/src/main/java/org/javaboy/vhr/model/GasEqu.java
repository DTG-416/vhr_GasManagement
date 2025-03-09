package org.javaboy.vhr.model;


public class GasEqu {

    private Integer id;
    private String code;
    private String processArea;
    private String name;
    private String pressureLevel;
    private String specification;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getProcessArea() {
        return processArea;
    }
    public void setProcessArea(String processArea) {
        this.processArea = processArea;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPressureLevel() {
        return pressureLevel;
    }
    public void setPressureLevel(String pressureLevel) {
        this.pressureLevel = pressureLevel;
    }
    public String getSpecification() {
        return specification;
    }
    public void setSpecification(String specification) {
        this.specification = specification;
    }
}

