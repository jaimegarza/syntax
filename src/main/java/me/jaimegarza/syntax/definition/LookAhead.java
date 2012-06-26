package me.jaimegarza.syntax.definition;

import java.util.HashSet;
import java.util.Set;

public class LookAhead {
  Set<Integer> symbolIds = new HashSet<Integer>();
  boolean carry = true;

  public Set<Integer> getSymbolIds() {
    return symbolIds;
  }

  public void setSymbolIds(Set<Integer> symbolIds) {
    this.symbolIds = symbolIds;
  }

  public boolean isCarry() {
    return carry;
  }

  public void setCarry(boolean carry) {
    this.carry = carry;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    
    try {
      LookAhead la = (LookAhead) obj;
      return carry == la.carry && symbolIds.equals(la.symbolIds);
    } catch (NullPointerException unused) {
      return false;
    } catch (ClassCastException unused) {
      return false;
    }
  }

}
