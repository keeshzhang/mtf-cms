package com.shenmao.vertx.starter.commons.files;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MyFindFileVisitor extends SimpleFileVisitor<Path> {

  private List<String> filenameList = new ArrayList<String>();
  private String fileSuffix = null;
//  private String[] fileSuffixArr = null;

  public MyFindFileVisitor(String fileSuffix) {
    this.fileSuffix = fileSuffix;
  }

//  public MyFindFileVisitor(String... fileSuffix) {
//    this.fileSuffixArr = fileSuffix;
//  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

    boolean isVisite = false;

    if ("*".equals(fileSuffix)) {
      isVisite = true;
    }

    if (!isVisite && fileSuffix != null && file.toString().endsWith(fileSuffix)) {
      isVisite = true;
    }

//    if (!isVisite && fileSuffixArr != null) {
//
//      for (String s : fileSuffixArr) {
//        if (file.toString().endsWith(s)) {
//          isVisite = true;
//          break;
//        }
//      }
//    }

    if (isVisite) {
      filenameList.add(file.toString());
    }

    return FileVisitResult.CONTINUE;
  }

  public List<String> getFilenameList() {
    return filenameList;
  }

  public void setFilenameList(List<String> filenameList) {
    this.filenameList = filenameList;
  }
}
