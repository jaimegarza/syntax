package me.jaimegarza.syntax.definition;

public class GoTo {

  private int origin;
  private int destination;

  public GoTo(int origin, int destination) {
    super();
    this.origin = origin;
    this.destination = destination;
  }

  public int getOrigin() {
    return origin;
  }

  public void setOrigin(int origin) {
    this.origin = origin;
  }

  public int getDestination() {
    return destination;
  }

  public void setDestination(int destination) {
    this.destination = destination;
  }
}
