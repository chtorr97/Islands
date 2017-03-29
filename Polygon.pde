class Polygon
{
  public ArrayList<Segment> edges;
  public ArrayList<Segment> intern;
  public ArrayList<Point> vertices;

  Polygon()
  {
    edges = new ArrayList<Segment>();
    intern = new ArrayList<Segment>();
    vertices = new ArrayList<Point>();
  }

  public void triangulate()
  {
    step(this);
  }

  private void step(Polygon tmp)
  {
    if (tmp.edges.size() > 3)
    {
      for (int k = 0; k < tmp.vertices.size(); k++)
      {
        Point a = tmp.vertices.get((k - 1 + tmp.edges.size()) % tmp.edges.size());
        Point b = tmp.vertices.get(k);
        Point c = tmp.vertices.get((k + 1) % tmp.edges.size());
        if (isClockwise(a, b, c))
        {
          boolean intersection = false;
          Segment current = new Segment(a, c);
          Segment test = new Segment(b, current.getMid());
          for (int t = 0; t < tmp.edges.size(); t++)
          {
            if (tmp.edges.get(t).intersects(current))
            {
              if (!tmp.edges.get(t).touches(current))
              {
                intersection = true;
                break;
              }
            }
          }
          
          for (int t = 0; t < tmp.edges.size(); t++)
          {
            if (tmp.edges.get(t).intersects(test))
            {
              if (!tmp.edges.get(t).touches(test))
              {
                intersection = true;
                break;
              }
            }
          }

          if (!intersection)
          {
            intern.add(new Segment(a, c));
            step(tmp.clipEar(b));
            break;
          }
        }
      }
    }
  }

  private boolean isClockwise(Point a, Point b, Point c)
  {
    float ab = (b.coordinate.x - a.coordinate.x) * (b.coordinate.y + a.coordinate.y);
    float bc = (c.coordinate.x - b.coordinate.x) * (c.coordinate.y + b.coordinate.y);
    float ca = (a.coordinate.x - c.coordinate.x) * (a.coordinate.y + c.coordinate.y);

    if (ab + bc + ca > 0)
      return false;
    else
      return true;
  }

  private void connect()
  {
    edges.clear();
    intern.clear();

    for (int k = 0; k < vertices.size(); k++)
    {
      edges.add(new Segment(vertices.get(k), vertices.get((k + 1) % vertices.size())));
    }

    for (int k = 0; k < edges.size(); k++)
    {
      edges.get(k).setNext(edges.get((k + 1) % edges.size()));
      edges.get(k).setPrev(edges.get((k - 1 + edges.size()) % edges.size()));
    }
  }

  public Polygon clipEar(Point a)
  {
    Polygon tmp = new Polygon();
    for (Point p : vertices)
    {
      if (!a.coordinate.equals(p.coordinate))
        tmp.vertices.add(p.clone());
    }

    tmp.connect();

    return tmp;
  }

  public Polygon clone()
  {
    Polygon tmp = new Polygon();
    for (Point p : vertices)
    {
      tmp.vertices.add(p.clone());
    }

    tmp.connect();

    return tmp;
  }
}