package me.jaimegarza.syntax.definition;


public abstract class Symbol {
  protected int id;
  protected String name;
  protected String fullName;
  protected int precedence;
  protected Associativity associativity;
  protected int count;
  protected int token;
  protected Type type;

  public Symbol(String name) {
    super();
    this.name = name;
    this.fullName = name;
    this.count = 0;
    this.token = -1;
    this.precedence = 0;
    this.associativity = Associativity.NONE;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public int getPrecedence() {
    return precedence;
  }

  public void setPrecedence(int precedence) {
    this.precedence = precedence;
  }

  public Associativity getAssociativity() {
    return associativity;
  }

  public void setAssociativity(Associativity associativity) {
    this.associativity = associativity;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getToken() {
    return token;
  }

  public void setToken(int token) {
    this.token = token;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    
    try {
      Symbol s = (Symbol) obj;
      return id == s.id && name.equals(s.name) && fullName.equals(s.fullName);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

  @Override
  public String toString() {
    return "\"" + name + "\"(" + id + ")";
  }

}
