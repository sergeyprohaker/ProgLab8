
package client.utils;
import common.exceptions.*;
import common.data.*;
import common.functional.WorkerPacket;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;



public class CommunicationControl {



    public CommunicationControl() {
    }



    public static boolean containsOnlyDigitsOrLetters(String str, boolean onlyDigits) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        String regex = onlyDigits ? "^\\d+$" : "^[a-zA-Z]+$";
        return str.matches(regex);
    }


    public String setName(String name) {
            try {
                if (name.equals("")) throw new EmptyInputException("имя не может быть пустым");
                if (!containsOnlyDigitsOrLetters(name, false)) throw new InputException();
            } catch (EmptyInputException | InputException e) {
                JOptionPane.showMessageDialog(null, "Неверное имя");
            }
        return name;
    }


    private long setHeight(String line) {
            try {
                long height = Long.parseLong(line);
                if ((height <= 0) || (height > 400)) {
                    throw new WrongArgumentsException("Высота не может быть меньше или равна нулю");
                }
                return height;
            } catch (Exception e) {
                System.out.println("Некорректный ввод. Попробуйте еще раз.");
            }
        return 0;
    }


    public String setPassportID(String passportId)  {
            try {
                if (passportId.isEmpty()) {
                    throw new EmptyInputException("Номер паспорта не может быть пустым");
                }
                if ((!passportId.matches("\\d+") || (passportId.length() != 6))) {
                    throw new WrongArgumentsException("Номер паспорта должен содержать только цифры(6 цифр)");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Неверные данные паспорта");
            }
        return passportId;
    }


    public LocalDateTime setBirthday(String birthdayStr) {
            try {
                if (birthdayStr.isEmpty()) throw new IllegalArgumentException();
                birthdayStr = birthdayStr.trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDateTime bd = LocalDate.parse(birthdayStr, formatter).atStartOfDay();
                if (bd.isAfter(LocalDate.now().atStartOfDay())) throw new WrongArgumentsException();
                return bd;
            } catch (WrongArgumentsException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        return null;
    }



    public Person setPerson(String birthday, String line, String passport, Location location) {
        try {
            LocalDateTime bDay = setBirthday(birthday);
            long height = setHeight(line);
            String passportID = setPassportID(passport);
            return new Person(bDay, height, passportID, location);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Неверные данные Person");
        }
        return null;
    }


    public Integer setCoodrinateX(String line) {
        int coordX;
            try {
                if (line.equals("")) throw new EmptyInputException("не может быть пустым");
                coordX = Integer.parseInt(line);
                if (coordX > 468) throw new InputException();
                return coordX;
            } catch (EmptyInputException e) {
                System.out.println(e.getMessage());
            } catch (InputException e) {
                JOptionPane.showMessageDialog(null, "превышено макс. значение (468)");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Должно быть целым числом");
            }
        return null;
    }

    public Integer setCoodrinateY(String line){
        Integer coordY;
            try {
                if (line.equals("")) throw new EmptyInputException();
                coordY = Integer.parseInt(line);
                if (coordY <= -922) throw new InputException();
                return coordY;
            } catch (EmptyInputException e) {
                JOptionPane.showMessageDialog(null, "Некорректные данные");
            } catch (InputException e) {
                JOptionPane.showMessageDialog(null, "Выход за минимальное значение (-992)");
            }
        return null;
    }


    public Double setSalary(String line) {
        double salary;
            try {
                if (line.equals("")) throw new EmptyInputException();
                salary = Double.parseDouble(line);
                if (salary <= 0) throw new InputException();
                return salary;
            } catch (EmptyInputException e) {
                JOptionPane.showMessageDialog(null, "Некорректный ввод");
            } catch (InputException e) {
                JOptionPane.showMessageDialog(null, "Зарплата должна быть больше 0");
            }

        return null;
    }


    public Coordinates setCoordinates(String coordX, String coordY) {
        Integer x;
        int y;
        x = setCoodrinateX(coordX);
        y = setCoodrinateY(coordY);

        return new Coordinates(x, y);

    }


    public Location setLocation(String xx, String yy, String zz, String name){
        float x;
        long y;
        int z;
        try {
            if (xx.equals("")) throw new Exception();
            x = Float.parseFloat(xx);


            if (yy.equals("")) throw new EmptyInputException();
            y = Long.parseLong(yy);


            if (zz.equals("")) throw new EmptyInputException();
            z = Integer.parseInt(zz);


            if (name.equals("")) throw new EmptyInputException();

            return new Location(x, y, z, name);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Неверный формат локации");
        }
        return null;
    }


    public Position choosePosition(String setPos) {
        Position position;
            try {
                position = Position.valueOf(setPos.toUpperCase());
                return position;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Неверные данные");
            }

        return null;
    }

    public Status chooseStatus(String setStat) {
        Status status;
            try {
                status = Status.valueOf(setStat.toUpperCase());
                return status;
            } catch (NoSuchElementException e) {
                JOptionPane.showMessageDialog(null, "Нет такого элемента");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ошибка данных");
            }
        return null;
    }

}
