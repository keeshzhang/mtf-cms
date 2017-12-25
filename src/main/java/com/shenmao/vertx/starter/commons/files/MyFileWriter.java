package com.shenmao.vertx.starter.commons.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MyFileWriter {

  private final String _resourceFolder;
  private final String _folder;

  public MyFileWriter(String folder) {
    this._resourceFolder = Paths.get(MyFileWriter.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(5)).getParent().toString() + "/classes";
    this._folder = _resourceFolder + "/" + folder;
  }

  public void appendLine(String line, String fileName) throws IOException {
    Files.write(Paths.get(fileName), ("\r\n" + line ).getBytes(), StandardOpenOption.APPEND);
  }

  public File getFile(String filename) {
    Path file_path = Paths.get(_folder + "/" + filename);
    File f = file_path.toFile();

    return f;
  }

  public File write(String content, String filename) {

    Path file_path = Paths.get(_folder + "/" + filename);
    File f = file_path.toFile();

    FileWriter fw = null;
    BufferedWriter bw = null;

    try {

      if (!f.getParentFile().exists()) {
        f.getParentFile().mkdirs();
      }

      if(!f.exists()){
        f.createNewFile();
      } else {
        return f;
      }

//      fw=new FileWriter(f.getAbsoluteFile(), true);  //true表示可以追加新内容
      fw=new FileWriter(f.getAbsoluteFile()); //表示不追加

      bw=new BufferedWriter(fw);
      bw.write(content);
      bw.close();

    }catch(Exception e){
      e.printStackTrace();
    }

    return f;

  }

}
