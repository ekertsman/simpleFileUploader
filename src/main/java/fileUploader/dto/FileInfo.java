package fileUploader.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class FileInfo implements Serializable {
    private static final long serialVersionUID = -1666823351845949724L;

    private String name;
    private String path;
    private long size;
}
