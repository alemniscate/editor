package editor;

import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.io.*;
import java.awt.image.*;
import javax.swing.filechooser.FileSystemView;

import java.util.ArrayList;
import java.util.regex.*;

public class TextEditor extends JFrame {
    private static final long serialVersionUID = 1L;
    static int findIndex = -1;
    static boolean initSearchFlag = true;
    static java.util.List<Selection> selectionList;
    static JFileChooser jfc;

    enum MODE {
        FIRST,
        NEXT,
        PREVIOUS
    }

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Text Editor");
        setBounds(100, 100, 500, 300);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JTextField searchField = new JTextField();
        searchField.setName("SearchField");
        searchField.setPreferredSize(new Dimension(120, 30));
        searchField.addActionListener(e -> initSearchFlag = true);
        JTextArea textArea = new JTextArea();
        textArea.setName("TextArea");
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        JCheckBox useRegex = CheckBox.create("UseRegExCheckbox", "Use regex");
        useRegex.addActionListener(e -> initSearchFlag = true);
        createButtons(panel, searchField, useRegex, textArea);
        add(panel, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.CENTER);
        createMenu(searchField, useRegex, textArea);
        jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setName("FileChooser");
        jfc.setVisible(false);
        add(jfc, BorderLayout.PAGE_END);
    }

    void createButtons(JPanel panel, JTextField searchField, JCheckBox useRegex, JTextArea textArea) {
        JButton openButton = Icon.create("OpenButton", "open.png", panel);
        JButton saveButton = Icon.create("SaveButton", "save.png", panel);
        panel.add(searchField);
        JButton searchButton = Icon.create("StartSearchButton", "search.png", panel);
        JButton previousButton = Icon.create("PreviousMatchButton", "previous.png", panel);
        JButton nextButton = Icon.create("NextMatchButton", "next.png", panel);
        panel.add(useRegex);

        openButton.addActionListener(e -> openFile(textArea));
        saveButton.addActionListener(e -> saveFile(textArea));
        searchButton.addActionListener(e -> searchText(MODE.FIRST, searchField, useRegex, textArea));
        previousButton.addActionListener(e -> searchText(MODE.PREVIOUS, searchField, useRegex, textArea));
        nextButton.addActionListener(e -> searchText(MODE.NEXT, searchField, useRegex, textArea));
    }

    void createMenu(JTextField searchField, JCheckBox useRegex, JTextArea textArea) {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        JMenuItem menuOpen = MenuItem.create("MenuOpen", "Open", fileMenu);
        JMenuItem menuSave = MenuItem.create("MenuSave", "Save", fileMenu);
        JMenuItem menuExit = MenuItem.create("MenuExit", "Exit", fileMenu);
        menuBar.add(fileMenu);
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        JMenuItem menuStartSearch = MenuItem.create("MenuStartSearch", "Start search", searchMenu);
        JMenuItem menuPreviousMatch = MenuItem.create("MenuPreviousMatch", "Previous match", searchMenu);
        JMenuItem menuNextMatch = MenuItem.create("MenuNextMatch", "Next match", searchMenu);
        JMenuItem menuUseRegExp = MenuItem.create("MenuUseRegExp", "Use regular expressions", searchMenu);
        menuUseRegExp.addActionListener(e -> useRegex.setSelected(true));
        menuBar.add(searchMenu);
        setJMenuBar(menuBar);
        
        menuOpen.addActionListener(e -> openFile(textArea));
        menuSave.addActionListener(e -> saveFile(textArea));
        menuExit.addActionListener(e -> dispose());

        menuStartSearch.addActionListener(e -> searchText(MODE.FIRST, searchField, useRegex, textArea));
        menuPreviousMatch.addActionListener(e -> searchText(MODE.PREVIOUS, searchField, useRegex, textArea));
        menuNextMatch.addActionListener(e -> searchText(MODE.NEXT, searchField, useRegex, textArea));
    }

    void searchText(MODE mode, JTextField searchField, JCheckBox useRegex, JTextArea textArea) {
        if (useRegex.isSelected()) {
            searchTextRegex(mode, searchField, textArea);
        } else {
            searchTextString(mode, searchField, textArea);
        } 
    }
    
    void searchTextString(MODE mode, JTextField searchField, JTextArea textArea) {
        String text = textArea.getText();
        String word = searchField.getText();

        if (initSearchFlag) {
            selectionList = new ArrayList<>();
            int index = text.indexOf(word);
            while (index >= 0) {
                selectionList.add(new Selection(index, index + word.length()));         
                index = text.indexOf(word, index + 1);
            }
            initSearchFlag = false;
        }
             
        if (selectionList.isEmpty()) {
            return;
        }  

        selectText(mode, textArea);
    }

    void searchTextRegex(MODE mode, JTextField searchField, JTextArea textArea) {
        String text = textArea.getText();
        String word = searchField.getText();

        if (initSearchFlag) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            selectionList = new ArrayList<>();
            while (matcher.find()) {
                selectionList.add(new Selection(matcher.start(), matcher.end()));                        
            }
            initSearchFlag = false;
        }
       
        if (selectionList.isEmpty()) {
            return;
        }  

        selectText(mode, textArea);
    }

    void selectText(MODE mode, JTextArea textArea) {
        
        switch (mode) {
            case FIRST:
                findIndex = 0;
                break;
            case NEXT:
                if (selectionList.size() - 1 >= findIndex + 1) {
                    findIndex++;
                } else {
                    findIndex = 0;
                }     
                break;
            case PREVIOUS:
                if (findIndex == 0) {
                    findIndex = selectionList.size() - 1;
                } else {
                    findIndex--;
                }
                break;
            default:
                break;
        }
   
        Selection selection = selectionList.get(findIndex);
   
        textArea.setCaretPosition(selection.getEnd());
        textArea.select(selection.getStart(), selection.getEnd());
        textArea.grabFocus();
    }

    void openFile(JTextArea textArea) {
        String openFileName = getOpenFileName();
        String text = ReadText.readAll(openFileName);
        textArea.setText(text);
        textArea.setVisible(true);
    } 

    void saveFile(JTextArea textArea) {
        String saveFileName = getOpenFileName();
        String text = textArea.getText();
        WriteText.writeAll(saveFileName, text);
    }

    String getOpenFileName() {
        
        String fileName = "";

        jfc.setVisible(true);
		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            fileName = selectedFile.getAbsolutePath();
			System.out.println(fileName);
        }
        
        return fileName;
    }

    public static void main(String[] args) {
        System.out.println(ReadText.getAbsolutePath("test.txt"));
        JFrame frame = new TextEditor();
        frame.setVisible(true);
    }
}

class ReadText {

    static boolean isExist(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    static String getAbsolutePath(String fileName) {
        File file = new File(fileName);
        return file.getAbsolutePath();
    }

    static String readAll(String fileName) {
        char[] cbuf = new char[4096];
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));           
            int offset = 0;
            while (true) {
                int length = br.read(cbuf, offset, cbuf.length);
                if (length != -1) {
                    offset += length;
                    sb.append(cbuf, 0, length);
                }
                if (length < cbuf.length) {
                    break;
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }
}

class WriteText {

    static void writeAll(String fileName, String text) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(text, 0, text.length());
            bw.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class Icon {

    Icon() {}

    static JButton create(String iconName, String fileName, JPanel panel) {
        JButton button = new JButton(load(fileName, 30, 30));
        button.setName(iconName);
        button.setPreferredSize(new Dimension(30, 30));
        panel.add(button);
        return button;
    }

    static ImageIcon load(String fileName, int width, int height) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Image dimg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        return new ImageIcon(dimg);
    }
}

class MenuItem {

    MenuItem() {}

    static JMenuItem create(String menuName, String displayName, JMenu menu) {
        JMenuItem menuItem = new JMenuItem(displayName);
        menuItem.setName(menuName);
        menu.add(menuItem);
        return menuItem;
    }
}

class CheckBox{

    CheckBox() {}

    static JCheckBox create(String checkBoxName, String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setName(checkBoxName);
        return checkBox;
    }
}

class Selection {
    private int start;
    private int end;

    Selection(int start, int end) {
        this.start = start;
        this.end = end;
    }

    int getStart() {
        return start;
    }

    int getEnd() {
        return end;
    }

    void setStart(int start) {
        this.start = start;
    }

    void setEnd(int end) {
        this.end = end;
    }
}