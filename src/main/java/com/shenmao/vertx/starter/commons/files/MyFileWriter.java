package com.shenmao.vertx.starter.commons.files;

import com.shenmao.vertx.starter.configuration.ApplicationConfig;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

public class MyFileWriter {

//  private static String _dbStorageFolder = "db_storage";
  private final String _appRoot;
  private final String _folder;

  public MyFileWriter(String folder) {
    this._appRoot =  ApplicationConfig.getAppRoot();
    this._folder = _appRoot + "/" + folder;
  }

  public static String readFile(String filefullname) throws IOException {
    return new String(Files.readAllBytes(Paths.get(filefullname)));
  }

  public static void moveFile(String from, String to) {

    try {

      Path p = Paths.get(to);

      writeFile(readFile(from), p.getParent().toString(), p.toFile().getName());

      System.out.println(from);
      System.out.println(to);

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void deleteFolder(String folder) {
    try {
      FileUtils.deleteDirectory(new File(folder));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void getAppResource(String folder_name) {

    // 将 resouce 下的同名文件下的所有内容递归复制到 app_root 下, 避免在 mvn clean 的时候删除 resource 的内容

    String resourceFolder = ApplicationConfig.getAppRoot() + "target/classes/" + folder_name;
    String rootFolder = ApplicationConfig.getAppRoot() + "/" + folder_name;

    MyFindFileVisitor findJavaVisitor = new MyFindFileVisitor("*");

    try {

      Path p = Paths.get(resourceFolder);

      if (!p.toFile().exists()) return;

      Files.walkFileTree(p, findJavaVisitor);

      findJavaVisitor.getFilenameList().stream().forEach(from -> {
        moveFile(from, rootFolder + from.replace(resourceFolder, ""));
      });

      deleteFolder(resourceFolder);

    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  public void appendLine(String line, String fileName) throws IOException {
    Files.write(Paths.get(fileName), ("\r\n" + line ).getBytes(), StandardOpenOption.APPEND);
  }

  public File getFile(String filename) {
    Path file_path = Paths.get(_folder + "/" + filename);
    File f = file_path.toFile();

    return f;
  }

  public static File writeFile(String content, String foldername, String fileName) {

    Path file_path = Paths.get(foldername + "/" + fileName);
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

  public File write(String content, String filename) {

    return writeFile(content, _folder, filename);

  }

}
