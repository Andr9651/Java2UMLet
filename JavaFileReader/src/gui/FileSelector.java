package gui;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;

import controller.JavaFileInterpreter;

public class FileSelector {

	private JFileChooser fileChooser;
	private Path savePath;
	private String lastSavePath;
	
	
	public static void main(String[] args) {
		new FileSelector();

	}
	
	public FileSelector() {
		
		savePath = Paths.get(System.getProperty("user.home") + "\\Java2UMLet\\");
		//System.out.println(System.getProperty("user.home") + "\\Java2UMLet\\");

		fileChooser = new JFileChooser();
		
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")+"\\git\\P2BevarUkraine\\P2.BevarUkraine"));
		
		fileChooser.setDialogTitle("Choose a file to convert");
		openFileChooser();

	}
	
	private void openFileChooser() {
		
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			
			JavaFileInterpreter reader = convertFiles(selectedFile);
			
			String clipboardString = "";
			
			for (String line : reader.getResultList()) {
				clipboardString += line + System.lineSeparator();
			}
			
			StringSelection selection = new StringSelection(clipboardString);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
			
			//open folder where the last file was saved
			/*
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(new File(lastSavePath));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			openFileChooser();
			
		}
	}
	
	private JavaFileInterpreter convertFiles(File file) {
		
		if (file.isDirectory()) {

			JavaFileInterpreter reader = null;
			
			File[] files = file.listFiles();
			if (files != null) {

				for (File listFile : files) {
					reader = convertFiles(listFile);
				}
			}
			
			return reader;
		} else {
			JavaFileInterpreter reader = new JavaFileInterpreter();
			reader.extractJavaFile(file);
		
			
			Path relativeSavePath = Paths.get(System.getProperty("user.home")).relativize(Paths.get(file.getParent()));
			
			
			
			lastSavePath = reader.saveResultFile(savePath + "\\" + relativeSavePath);
			
			fileChooser.setDialogTitle("File converted! result in clipboard and in: " + savePath + "\\" + relativeSavePath);
			
			return reader;
		}
		
				
	}

}
