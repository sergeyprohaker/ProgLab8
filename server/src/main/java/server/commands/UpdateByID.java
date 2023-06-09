
package server.commands;

import common.data.*;
import common.exceptions.*;
import common.functional.User;
import common.functional.WorkerPacket;
import server.RunServer;
import server.utils.*;

import java.time.ZonedDateTime;

/**
 * This command is used to update the value of a collection element whose id matches the specified one.
 */

public class UpdateByID extends AbstractCommand {
    CollectionControl collectionControl;
    DatabaseCollectionManager databaseCollectionManager;

    public UpdateByID(CollectionControl collectionControl, DatabaseCollectionManager databaseCollectionManager) {
        super("update_by_id", "Обновить значение элемента коллекции, id  которого равен заданному");
        this.collectionControl = collectionControl;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Executes the command with the specified argument
     *
     * @param argument the id of the element to be updated
     */
    @Override
    public boolean execute(String argument, Object commandObjectArgument, User user) {
        try {
            if (argument.isEmpty() || commandObjectArgument == null) throw new WrongArgumentsException();
            if (collectionControl.collectionSize() == 0) throw new CollectionIsEmptyException();

            int id = Integer.parseInt(argument);
            System.out.println(id);
            if (id <= 0) throw new NumberFormatException();
            Worker oldWorker = collectionControl.getById(id);
            if (oldWorker == null) throw new WorkerNotFoundException();
            if (!oldWorker.getOwner().equals(user)) throw new PermissionsDeniedException();
            if (!databaseCollectionManager.checkWorkerUserId(oldWorker.getId(), user)) throw new ManualDatabaseEditException();
            WorkerPacket workerPacket = (WorkerPacket) commandObjectArgument;
            databaseCollectionManager.updateWorkerById(id, workerPacket);
            RunServer.logger.info("вышли из updateWorkerById");
            String name = workerPacket.getName() == null ? oldWorker.getName() : workerPacket.getName();
            Coordinates coordinates = workerPacket.getCoordinates() == null ? oldWorker.getCoordinates() : workerPacket.getCoordinates();
            ZonedDateTime creationDate = oldWorker.getCreationDate();
            double salary = workerPacket.getSalary() == null ? oldWorker.getSalary() : workerPacket.getSalary();
            Position position = workerPacket.getPosition() == null ? oldWorker.getPosition() : workerPacket.getPosition();
            Status status = workerPacket.getStatus() == null ? oldWorker.getStatus() : workerPacket.getStatus();
            Person person = workerPacket.getPerson() == null ? oldWorker.getPerson() : workerPacket.getPerson();
            System.out.println(collectionControl.getCollection());
            collectionControl.removeFromCollection(oldWorker);
//            if (!collectionControl.getCollection().contains(oldWorker)) {
            System.out.println(collectionControl.getCollection());
            collectionControl.addToCollection(new Worker(id,
                    name,
                    coordinates,
                    creationDate,
                    salary,
                    position,
                    status,
                    person,
                    user));
            ResponseOutputer.appendln("Замена успешно завершена!");
            System.out.println(collectionControl.getCollection());
            return true;
//            }
//            return false;
        } catch (WrongArgumentsException e) {
            ResponseOutputer.appendln(e.getMessage());
        } catch (NumberFormatException e) {
            ResponseOutputer.appendln("неправильный тип данных. Должен быть целочисленным");
            e.printStackTrace();
        } catch (CollectionIsEmptyException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        } catch (PermissionsDeniedException e) {
            RunServer.logger.error("Доступ к объекту запрещен, он не ваш!");
            e.printStackTrace();
        } catch (ManualDatabaseEditException e) {
            RunServer.logger.error("Прямое изменение базы!");
            e.printStackTrace();
        } catch (WorkerNotFoundException e) {
            RunServer.logger.error("Рабочий не найден");
            e.printStackTrace();
        } catch (DatabaseHandlingException e) {
            e.printStackTrace();
            RunServer.logger.error("Ошибка при обращении к БД");
        }
        return false;
    }
}
