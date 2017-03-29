class Segment
{
  private Point first;
  private Point second;

  private Segment next;
  private Segment prev;

  Segment(Point first, Point second)
  {
    this.first = first;
    this.second = second;
  }

  public Point getFirst()
  {
    return first;
  }

  public Point getSecond()
  {
    return second;
  }

  public Segment getNext()
  {
    return next;
  }

  public Segment getPrev()
  {
    return prev;
  }

  public void setNext(Segment next)
  {
    this.next = next;
  }

  public void setPrev(Segment prev)
  {
    this.prev = prev;
  }
  
  //From http://ideone.com/PnPJgb
  public boolean intersects(Segment other)
  {
    PVector A = first.coordinate;
    PVector B = second.coordinate;
    PVector C = other.first.coordinate;
    PVector D = other.second.coordinate;

    PVector CmP = new PVector(C.x - A.x, C.y - A.y);
    PVector r = new PVector(B.x - A.x, B.y - A.y);
    PVector s = new PVector(D.x - C.x, D.y - C.y);

    float CmPxr = CmP.x * r.y - CmP.y * r.x;
    float CmPxs = CmP.x * s.y - CmP.y * s.x;
    float rxs = r.x * s.y - r.y * s.x;

    if (CmPxr == 0f)
    { 
      return ((C.x - A.x < 0) != (C.x - B.x < 0))
        || ((C.y - A.y < 0) != (C.y - B.y < 0));
    }

    if (rxs == 0f)
      return false;

    float rxsr = 1f / rxs;
    float t = CmPxs * rxsr;
    float u = CmPxr * rxsr;

    return (t >= 0f) && (t <= 1f) && (u >= 0f) && (u <= 1f);
  }

  public boolean touches(Segment s)
  {
    if (first.coordinate.equals(s.first.coordinate) || second.coordinate.equals(s.second.coordinate) ||
      second.coordinate.equals(s.first.coordinate) || first.coordinate.equals(s.second.coordinate))
      return true;
    return false;
  }

  public boolean is(Segment s)
  {
    if ((first.coordinate.equals(s.first.coordinate) && second.coordinate.equals(s.second.coordinate)) ||
      (second.coordinate.equals(s.first.coordinate) && first.coordinate.equals(s.second.coordinate)))
      return true;
    return false;
  }
  
  public Point getMid()
  {
    return new Point((first.coordinate.x + second.coordinate.x) / 2, (first.coordinate.y + second.coordinate.y) / 2);
  }

  public Segment clone()
  {
    return new Segment(first.clone(), second.clone());
  }

  public void draw()
  {
    line(first.coordinate.x, first.coordinate.y, second.coordinate.x, second.coordinate.y);
  }
}