package fr.tvbarthel.apps.cameracolorpicker.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class FileSystemController {

    public boolean checkFileExist(String fileName) {
        File fs = new File(fileName);
        if (fs.exists()) {
            return true;
        }
        else {
            return false;
        }
    }

    public String openFileByRead(String fileName) {
        if (!checkFileExist(fileName)) {
            return null;
        }
        File fs = new File(fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fs);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String str;
        try {
            str = br.readLine();
            return str;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean openFileByWrite(String fileName, String data) {
        File fs = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(fs);
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteFile(String fileName) {
        if (!checkFileExist(fileName)) {
            return true;
        }
        File fs = new File(fileName);
        try {
            if (fs.delete()) {
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
