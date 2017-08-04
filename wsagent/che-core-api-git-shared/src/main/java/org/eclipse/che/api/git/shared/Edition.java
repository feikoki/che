package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Edition {
    int getBeginLine();

    void setBeginLine(int startLine);

    Edition withBeginLine(int startLine);

    int getEndLine();

    void setEndLine(int endLine);

    Edition withEndLine(int endLine);

//    int[] getLines();
//
//    void setLines(int[] lines);
//
//    Edition withLines(int[] lines);

    String getType();

    void setType(String type);

    Edition withType(String type);
}
