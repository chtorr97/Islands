class Point
{
  public PVector coordinate;
  public float hCost = 0;
  public float gCost = 0;
  public int searchID = 0;
  public Point parent;

  public ArrayList<Point> reachable;
  
  public boolean isClosed = false;

  Point()
  {
    this.coordinate = new PVector(0, 0);
    reachable = new ArrayList<Point>();
  }

  Point(PVector coordinate)
  {
    this.coordinate = coordinate;
    reachable = new ArrayList<Point>();
  }

  Point(float x, float y)
  {
    this.coordinate = new PVector(x, y);
    reachable = new ArrayList<Point>();
  }

  public float getF()
  {
    return gCost + hCost;
  }

  public float getDistance(Point p)
  {
    return coordinate.dist(p.coordinate);
  }

  public Point clone()
  {
    Point tmp = new Point(coordinate.copy());
    tmp.hCost = hCost;
    tmp.gCost = gCost;
    return tmp;
  }

  public boolean samePlace(Point p)
  {
    if ((coordinate.x == p.coordinate.x) && (coordinate.y == p.coordinate.y))
      return true;
    return false;
  }
}