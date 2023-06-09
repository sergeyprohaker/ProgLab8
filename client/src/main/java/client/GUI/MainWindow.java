package client.GUI;
import common.exceptions.InputException;
import common.exceptions.WrongArgumentsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.swing.table.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import client.Client;
import client.GUI.sort.SortingAndFilteringParameters;
import client.GUI.sort.WorkerSortingPanel;
import client.GUI.sort.WorkerUtils;
import client.utils.CommunicationControl;
import client.utils.PasswordHasher;
import common.data.*;
import common.functional.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainWindow extends JFrame {
    private final Map<Integer, Worker> rowToObjectMap = new HashMap<>();
    private static JTable table;
    private ArrayList<ArrayList<Worker>> workersss;
    public static int rowCounter = 0;
    public String selectedCommand;
    JButton saveButton;
    boolean actionBool = true;
    private static ArrayList<Worker> takeArray;
    CommunicationControl communicationControl;
    private ResourceBundle messages = ResourceBundle.getBundle("client.GUI.Messages", UserSettings.getInstance().getSelectedLocale());

    private ArrayList<Integer> workersID = new ArrayList<>();



    public MainWindow(Client client, CommunicationControl communicationControl) {
        setTitle(messages.getString("mainWindow"));
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);




        WorkerSortingPanel workerSortingPanel = new WorkerSortingPanel();

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel1.setBackground(Color.WHITE);
        panel1.setBorder(BorderFactory.createTitledBorder(""));

        JLabel userLabel = new JLabel("USER: " + client.getCurrentUser().getUsername());
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        userLabel.setBorder(border);

        JButton goToVisualisationTableButton = new JButton(messages.getString("visualizeObject"));
        goToVisualisationTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VisualTable visualTable = new VisualTable(client, communicationControl, workersss);
                visualTable.setVisible(true);
                dispose();
            }
        });
        JButton settings = new JButton();
        ImageIcon icon = new ImageIcon("C:\\Users\\Sergey\\IdeaProjects\\ProgLab8\\client\\settingsPNG.png");
        Image scaledImage = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        settings.setIcon(scaledIcon);
        settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsForm(client, communicationControl);
                dispose();
            }
        });




        panel1.add(userLabel, BorderLayout.WEST);
        panel1.add(settings, BorderLayout.SOUTH);
        panel1.add(goToVisualisationTableButton, BorderLayout.SOUTH);



        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel2.setBackground(Color.LIGHT_GRAY);
        panel2.setBorder(BorderFactory.createTitledBorder("Workers table"));
        String[] columnNames = {"id", messages.getString("name"), messages.getString("coordX"), messages.getString("coordY"),
                messages.getString("creationDate"), messages.getString("salary"), messages.getString("position"),
                messages.getString("status"), messages.getString("birthday"), messages.getString("height"),
                messages.getString("passport"), messages.getString("locX"), messages.getString("locY"),
                messages.getString("locZ"), messages.getString("locName"), "editable"};
        System.out.println(columnNames.length);
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                if (column == this.getColumnCount() - 1) { // Последняя ячейка в строке
                    Worker worker = rowToObjectMap.get(row);
                    if (worker.getOwner().equals(client.getCurrentUser())) {
                        comp.setBackground(Color.GREEN);
                    } else {
                        comp.setBackground(Color.RED);
                    }
                } else {
                    comp.setBackground(Color.WHITE);
                }

                return comp;
            }
        };
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                actionBool = false;
                if (e.getClickCount() == 1) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    if (row != -1) {
                        EditWorker editWorker = new EditWorker(communicationControl);
                        // код для перехода на другой
                        Worker worker = rowToObjectMap.get(row);
                        editWorker.setInfo(worker);
                        if (!worker.getOwner().equals(client.getCurrentUser())) {
                            editWorker.setNonEditable();
                        } else {
                            editWorker.setEditable();
                        }
                        saveButton = editWorker.getSaveButton();

                        saveButton.addActionListener(e1 -> {
                            try {
                                System.out.println(worker.getId());
                                client.sendRequest(new Request("update_by_id", String.valueOf(worker.getId()), editWorker.update(), client.getCurrentUser()));
                                Response res = client.receiveResponse();
                                System.out.println(res.getResponseBody() + " " + res.getResponseCode() );
                                editWorker.dispose();
                            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                        actionBool = true;
                    }else{
                        System.out.println();
                    }
                }
            }
        });


        /////
//        Executors.newSingleThreadExecutor()
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(() -> {
            if (actionBool) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        // ваш код

                        this.workersss = new ArrayList<>();

                        // Здесь происходит обновление данных в таблице
                        client.sendRequest(new Request("sendNewList", "", client.getCurrentUser()));
                        Response response = client.receiveResponse();
                        try{
                            int count = (Integer) response.getResponseObject();
                            for (int i = 0; i < count; i ++){
                                Response tempResponse = client.receiveResponse();
                                workersss.add((ArrayList<Worker>) tempResponse.getResponseObject());
                            }
                        } catch (Exception ex) {
                        }

                        clearData(tableModel);
                        // Очистка модели данных таблицы

                        rowCounter = 0;
                        ArrayList<Worker> newJoinedArray = new ArrayList<>();

                        for (ArrayList<Worker> list: workersss){
                            newJoinedArray.addAll(list);
                        }
                        setWorkersss(newJoinedArray);

                        WorkerUtils.parameters = workerSortingPanel.getParamaters();
                        newJoinedArray = WorkerUtils.sortAndFilterWorkers(newJoinedArray);
                        for (Worker worker : newJoinedArray) {
                            Object[] rowData = {String.valueOf(worker.getId()), String.valueOf(worker.getName()), String.valueOf(worker.getCoordinates().getX()),
                                    String.valueOf(worker.getCoordinates().getY()), String.valueOf(worker.getCreationDate()), String.valueOf(worker.getSalary()),
                                    String.valueOf(worker.getPosition()), String.valueOf(worker.getStatus()), String.valueOf(worker.getPerson().getBirthday()),
                                    String.valueOf(worker.getPerson().getHeight()), String.valueOf(worker.getPerson().getPassportID()),
                                    String.valueOf(worker.getPerson().getLocation().getX()),
                                    String.valueOf(worker.getPerson().getLocation().getY()), String.valueOf(worker.getPerson().getLocation().getZ()),
                                    String.valueOf(worker.getPerson().getLocation().getName()), ""};

                            rowToObjectMap.put(tableModel.getRowCount(), worker);
                            tableModel.addRow(rowData);
                        }

                        // Уведомление таблицы об изменении данных
                        tableModel.fireTableDataChanged();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }, 0, 2, TimeUnit.SECONDS);
//        Timer timer = new Timer(2000, e -> {
//            try {
//                if (actionBool) {
//                    this.workersss = new ArrayList<>();
//
//                    // Здесь происходит обновление данных в таблице
//                    client.sendRequest(new Request("sendNewList", "", client.getCurrentUser()));
//                    Response response = client.receiveResponse();
//                    try{
//                        int count = (Integer) response.getResponseObject();
//                        for (int i = 0; i < count; i ++){
//                            Response tempResponse = client.receiveResponse();
//                            workersss.add((ArrayList<Worker>) tempResponse.getResponseObject());
//                        }
//                    } catch (Exception ex) {
//                    }
//
//
//                    clearData(tableModel);
//                    // Очистка модели данных таблицы
//
//                    rowCounter = 0;
//                    ArrayList<Worker> newJoinedArray = new ArrayList<>();
//
//                    for (ArrayList<Worker> list: workersss){
//                        newJoinedArray.addAll(list);
//                    }
//                    setWorkersss(newJoinedArray);
//
//
//                    WorkerUtils.parameters = workerSortingPanel.getParamaters();
//                    newJoinedArray = WorkerUtils.sortAndFilterWorkers(newJoinedArray);
//                    for (Worker worker : newJoinedArray) {
//                        Object[] rowData = {String.valueOf(worker.getId()), String.valueOf(worker.getName()), String.valueOf(worker.getCoordinates().getX()),
//                                String.valueOf(worker.getCoordinates().getY()), String.valueOf(worker.getCreationDate()), String.valueOf(worker.getSalary()),
//                                String.valueOf(worker.getPosition()), String.valueOf(worker.getStatus()), String.valueOf(worker.getPerson().getBirthday()),
//                                String.valueOf(worker.getPerson().getHeight()), String.valueOf(worker.getPerson().getPassportID()),
//                                String.valueOf(worker.getPerson().getLocation().getX()),
//                                String.valueOf(worker.getPerson().getLocation().getY()), String.valueOf(worker.getPerson().getLocation().getZ()),
//                                String.valueOf(worker.getPerson().getLocation().getName()), ""};
//
//                        rowToObjectMap.put(tableModel.getRowCount(), worker);
//                        tableModel.addRow(rowData);
//                    }
//
//
//                    // Уведомление таблицы об изменении данных
//                    tableModel.fireTableDataChanged();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                System.out.println("");
//            }
//        });
//
//// Запуск таймера
//        timer.start();









        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1000, 300));
        scrollPane.setBackground(Color.MAGENTA);
        panel2.add(scrollPane, BorderLayout.WEST);



        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel3.setBackground(Color.WHITE);
        panel3.setBorder(BorderFactory.createTitledBorder(""));
        String[] commands = {"", "addElement", "add_if_min", "clear", "info", "print_field_ascending_person", "remove_element_by_id", "remove_greater", "update_by_id", "execute_script"};

        // Создаем выпадающий список и добавляем в панель
        JComboBox<String> commandList = new JComboBox<>(commands);

        panel3.add(commandList, BorderLayout.CENTER);
        JButton commandButton = new JButton(messages.getString("start"));

        commandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (selectedCommand) {
                    case "":
                        break;
                    case "addElement":
                    case "add_if_min":
                        actionBool = false;

                        EditWorker addWorker = new EditWorker(communicationControl);
                        saveButton = addWorker.getSaveButton();
                        saveButton.addActionListener(e1 -> {
                            try {

                                if (Objects.equals(selectedCommand, "addElement")) {
                                    client.sendRequest(new Request("addElement", "", addWorker.update(), client.getCurrentUser()));
                                }else{
                                    client.sendRequest(new Request("add_if_min", "", addWorker.update(), client.getCurrentUser()));
                                }
                                Response response777;
                                response777 = client.receiveResponse();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            } catch (ClassNotFoundException ex) {
                                throw new RuntimeException(ex);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }

                        });
                        actionBool = true;
                        break;
                    case "clear":
                        actionBool = false;
                        try {
                            client.sendRequest(new Request("clear", "", null, client.getCurrentUser()));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        Response response444;
                        try {
                            response444 = client.receiveResponse();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }

                        JOptionPane.showMessageDialog(null, response444.getResponseCode());
                        actionBool = true;
                        break;
                    case "info":
                        actionBool = false;
                        try {
                            client.sendRequest(new Request("info", "", null, client.getCurrentUser()));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                        Response response228;
                        try {
                            response228 = client.receiveResponse();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        System.out.println(response228.getResponseBody() + " " + response228.getResponseCode());
                        System.out.println(response228.getResponseBody());
                        JOptionPane.showMessageDialog(null, response228.getResponseBody());
                        actionBool = false;
                        break;
                    case "print_field_ascending_person":
                        actionBool = false;
                        try {
                            client.sendRequest(new Request("print_field_ascending_person", "", null, client.getCurrentUser()));
                            Response response1;
                            response1 = client.receiveResponse();
                            System.out.println(response1.getResponseBody());
                            JOptionPane.showMessageDialog(null, response1.getResponseBody());
                        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        actionBool = true;
                        break;
                    case "remove_element_by_id":
                        actionBool = false;
                        String inputRemId = JOptionPane.showInputDialog(null, messages.getString("insertArgument"));

                        try {
                            client.sendRequest(new Request("remove_element_by_id", inputRemId, null, client.getCurrentUser()));
                            Response response2;
                            response2 = client.receiveResponse();
                            System.out.println(response2);
                            JOptionPane.showMessageDialog(null, response2.getResponseBody());
                        } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        actionBool = true;
                        break;
                    case "remove_greater":
                        actionBool = false;
                        EditWorker removeGreater = new EditWorker(communicationControl);
                        saveButton = removeGreater.getSaveButton();

                        saveButton.addActionListener(e1 -> {
                            Response eshkere;
                            try {

                                client.sendRequest(new Request("remove_greater", "", removeGreater.update(), client.getCurrentUser()));
                                eshkere = client.receiveResponse();
                            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            JOptionPane.showMessageDialog(null, eshkere.getResponseBody());

                        });
                        actionBool = true;
                        break;
                    case "update_by_id":
                        actionBool = false;
                        String inputID = JOptionPane.showInputDialog(null, messages.getString("insertArgument"));

                        EditWorker update_by_id = new EditWorker(communicationControl);
                        saveButton = update_by_id.getSaveButton();
                        saveButton.addActionListener(e1 -> {
                            Response resp;
                            try {

                                client.sendRequest(new Request("update_by_id", inputID, update_by_id.update(), client.getCurrentUser()));
                                resp = client.receiveResponse();
                            } catch (IOException | ClassNotFoundException | InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                            JOptionPane.showMessageDialog(null, resp.getResponseBody());

                        });
                        actionBool = true;
                        break;

                    case "execute_script":
                        actionBool = false;
//                        Thread myThread = new Thread(() -> {
                        String filePath = JOptionPane.showInputDialog(null, messages.getString("insertPath"));
                        System.out.println(client.processScriptToServer(new File(filePath)));
//                        });
//                        myThread.start();
                        actionBool = true;
                        break;
                    default:
                        System.out.println("gg");
                        break;
                }
            }
        });


        commandList.addActionListener(e -> {
            // Получаем выбранную команду
            selectedCommand = (String) commandList.getSelectedItem();

            // Вызываем метод, передавая выбранную команду в качестве аргумента
            System.out.println(selectedCommand);
        });

        panel3.add(commandButton, BorderLayout.SOUTH);
        JPanel idPanel = new JPanel(new BorderLayout());




        //добавление панелей
        JPanel contentPane = new JPanel(new BorderLayout());
        JPanel lowPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.WHITE);
        contentPane.add(panel1, BorderLayout.NORTH);

        contentPane.add(panel2, BorderLayout.CENTER);

        lowPane.add(panel3, BorderLayout.NORTH);
        lowPane.add(workerSortingPanel, BorderLayout.WEST);
        contentPane.add(lowPane, BorderLayout.SOUTH);
        add(contentPane);

        pack();
    }

    public void clearData(DefaultTableModel model) {
        model.setRowCount(0);

    }

    public static  ArrayList<Worker> getWorkersss(){
        return takeArray;
    }

    public static void setWorkersss(ArrayList<Worker> aWorkers){
        takeArray = aWorkers;
    }
}

