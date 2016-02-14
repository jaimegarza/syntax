package me.jaimegarza.syntax.model.nfa;

public class Construct {
  private Node start;
  private Node end;

  public Construct(Node start, Node end) {
    super();
    this.start = start;
    this.end = end;
  }

  /**
   * @return the start
   */
  public Node getStart() {
    return start;
  }

  /**
   * @return the end
   */
  public Node getEnd() {
    return end;
  }
}
