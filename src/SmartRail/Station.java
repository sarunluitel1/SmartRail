/************************************
 @author Vincent Huber
 This class is used to run a station for smartRail


 ************************************/


package SmartRail;

import java.util.LinkedList;

public class Station extends Thread implements Component
{
  //adjacent track to the station.
  private static int totalStation = 0;
  private Track rightTrack;//expects pointer to a track
  private Track leftTrack;
  private String stationName; //expects Names in format St.15
  private Train trainInStation = null;
  private volatile LinkedList<Message> messages = new LinkedList<>();
  private boolean secured = false;

  //Constructor for station
  public Station()
  {
    totalStation++;
    this.stationName = "Station " + totalStation;
  }


  // Setters for data types
  public void setRightTrack(Track rightTrack)
  {
    this.rightTrack = rightTrack;
  }

  public void setLeftTrack(Track leftTrack)
  {
    this.leftTrack = leftTrack;
  }

  /* This method is used by a train on the station so the train
   * can give its own reference to the station
   * takes a Train parameter and returns nothing
   */
  @Override
  public void getTrainId(Train t)
  {
    trainInStation = t;
  }

  /* Code used when a train is leaving a station
   * No parameters and no return
   * this method also frees up the station to be secured for another component
   */
  @Override
  public void trainLeaving()
  {
    secured = false;
    trainInStation = null;
  }

  /* This method is used by the Train to determine the next component in the path
   * This method takes in a String parameter and returns the correct next Component
   */
  public Component nextComponent(String Direction)
  {
    if (Direction.equalsIgnoreCase("right")) return rightTrack;
    return leftTrack;

  }

  /* acceptMessage is a method used by the components to read in a message
   * the message received is added to the end of the queue (linkedlist) of
   * messages
   * Message parameter, no return
   */
  @Override
  public synchronized void acceptMessage(Message mes)
  {
    System.out.println("Message received: " + stationName);
    messages.add(mes);
    System.out.println(messages.getFirst().getAction());
    System.out.println(messages.size());
    //System.out.println(message.getDirection());
    notifyAll();
  }

  /* This method is used to send a findPath message along to a correct station
   * This method has a component and a String (direction) for parameters
   * returns a boolean for processing purposes
   */
  @Override
  public boolean findPath(Component c, String dir)
  {
    LinkedList<Component> compList = new LinkedList<>();
    if (dir.equalsIgnoreCase("right") && rightTrack != null)
    {
      compList.add(c);
      rightTrack.acceptMessage(new Message(dir, "findpath", compList, this));
      return true;
    } else if (dir.equalsIgnoreCase("left") && leftTrack != null)
    {
      compList.add(c);
      leftTrack.acceptMessage(new Message(dir, "findpath", compList, this));
      return true;
    }
    return false;
  }

  /* This method is used to send a returnPath message along to a correct station
  * This method has a component and a String (direction) for parameters
  * returns a boolean for processing purposes
  */
  @Override
  public boolean returnPath(Message m)
  {
    String dir = m.getDirection();

    if (trainInStation != null)
    {
      System.out.println("Not null");
      //Do something
    } else if (dir.equalsIgnoreCase("right") && rightTrack != null)
    {
      rightTrack.acceptMessage(m);
      return true;
    } else if (dir.equalsIgnoreCase("left") && leftTrack != null)
    {
      leftTrack.acceptMessage(m);
      return true;
    }
    return false;

  }

  /* This method is used to send a securePath message along to a correct station
 * This method has a component and a String (direction) for parameters
 * returns a boolean for processing purposes
 */
  @Override
  public synchronized boolean securePath(Message m)
  {
    String dir = m.getDirection();
    if (secured)
    {
      if (m.getSender() == trainInStation)
      {
        trainInStation.acceptMessage(new Message(dir, "couldNotSecure", new LinkedList<>(), this));
      }
      else if (dir.equalsIgnoreCase("right"))
      {
        leftTrack.acceptMessage(new Message("left", "couldNotSecure", new LinkedList<>(), this));
      }
      else
      {
        rightTrack.acceptMessage(new Message("right", "couldNotSecure", new LinkedList<>(), this));
      }
    }
    secured = true;
    System.out.println("Secure " + stationName);
    //System.out.println(m.getTarget().getLast().getComponentName());
    m.getTarget().remove(m.getTarget().size() - 1);
    //System.out.println(m.getTarget().getLast().getComponentName());
    messages.remove();
    if (dir.equalsIgnoreCase("right"))
    {
      rightTrack.acceptMessage(m);
    } else
    {
      leftTrack.acceptMessage(m);
    }
    return true;
  }

  /* This method is used to send a readyfortrain message along to the correct train
 * This method has a component and a String (direction) for parameters
 * returns a boolean for processing purposes
 */
  @Override
  public synchronized boolean readyForTrain(Message m)
  {
    if (trainInStation != null)
    {

    }
    System.out.println("sending back");
    return false;
  }

  /* This method is used to send a couldnotsecure message along to the correct train
 * This method has a component and a String (direction) for parameters
 * returns a boolean for processing purposes
 */
  @Override
  public synchronized boolean couldNotSecure(Message m)
  {
    secured = false;
    m.setSender(this);
    if(trainInStation != null)
    {
      trainInStation.acceptMessage(m);
    }
    return false;
  }

  /* The station processes the message and correctly generates a return message
   * or uses the corretc method to process the message
   * No parameters or return
   */
  @Override
  public void run()
  {

    while (true)
    {
      synchronized (this)
      {
        if (messages.isEmpty())
        {
          try
          {
            wait();
          } catch (Exception ex)
          {
            //Print
          }
        } else
        {
          //System.out.println("Past wait");
          String newDir;
          String action = messages.getFirst().getAction();
          String direction = messages.getFirst().getDirection();
          LinkedList<Component> target = messages.getFirst().getTarget();
          if (action.equalsIgnoreCase("findpath"))
          {
            if (target.get(0).getComponentName().equalsIgnoreCase(stationName))
            {
              System.out.println(stationName + " found.");
              LinkedList<Component> pathList = new LinkedList<>();
              if (direction.equalsIgnoreCase("right"))
              {
                newDir = "left";
              } else
              {
                newDir = "right";
              }
              pathList.add(this);
              Message correctStation = new Message(newDir, "returnpath", pathList, this);
              returnPath(correctStation);
              System.out.println("Sending return path");
            } else if (findPath(target.get(0), direction))
            {
              if (direction.equalsIgnoreCase("right"))
              {
                System.out.println("Sending messsage to track: " + rightTrack.getComponentName());
              } else
              {
                System.out.println("Sending messsage to track: " + leftTrack.getComponentName());
              }
              //System.out.println(stationName + " found.");
            } else
            {
              LinkedList<Component> emptyList = new LinkedList<>();
              if (direction.equalsIgnoreCase("right"))
              {
                newDir = "left";
              } else
              {
                newDir = "right";
              }
              Message wrongStation = new Message(newDir, "returnpath", emptyList, this);
              returnPath(wrongStation);
              System.out.println("Sending return path");

            }
            messages.remove();
          } else if (action.equalsIgnoreCase("returnpath"))
          {
            if (trainInStation != null)
            {
              System.out.println("Train on station");
              if (!target.isEmpty())
              {
                messages.getFirst().getTarget().add(this);
              }
              trainInStation.acceptMessage(messages.getFirst());
              messages.remove();

            }
          } else if (action.equalsIgnoreCase("securepath"))
          {
            System.out.println(target.getFirst().getComponentName());
            if (target.getFirst().getComponentName().equalsIgnoreCase(stationName))
            {
              System.out.println("HERE");
              secured = true;
              if (direction.equalsIgnoreCase("right"))
              {
                newDir = "left";
              } else
              {
                newDir = "right";
              }
              Message pathSecured = new Message(newDir, "readyfortrain", target, this);
              if (newDir.equalsIgnoreCase("left"))
              {
                leftTrack.acceptMessage(pathSecured);
              } else
              {
                rightTrack.acceptMessage(pathSecured);
              }
              messages.remove();
            } else
            {
              securePath(messages.getFirst());
            }
            //====================================

            //====================================
          } else if (action.equalsIgnoreCase("readyfortrain"))
          {
            if (trainInStation != null)
            {
              trainInStation.acceptMessage(messages.getFirst());

              messages.remove();
            }
          }
          else if (action.equalsIgnoreCase("couldnotsecure"))
          {
            couldNotSecure(messages.getFirst());
            messages.remove();
          }
        }
      }
      //System.out.println("Here");
    }

  }

  /* This method send a message to a train which direction the
   * train needs to look to for its destination
   * returns a string with no parameters
   */
  public String directionOut()
  {
    if (rightTrack != null)
    {
      return "right";
    }
    return "left";
  }

  /* Method used for display purposes
   * boolean return
   */
  public boolean hasTrain()
  {
    if(trainInStation != null)
    {
      return true;
    }
    return false;
  }

  /* returns a string name of the component
   * no parameters
   */
  public String getComponentName()
  {
    return this.stationName;
  }
}
