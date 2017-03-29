class Pathfinder
{
  public ArrayList<Polygon> islands;
  public ArrayList<PVector> path;
  private int currentSearch = 0;
  private ArrayList<Point> openList;

  Pathfinder()
  {
    islands = new ArrayList<Polygon>();
    path = new ArrayList<PVector>();
    openList = new ArrayList<Point>();
  }

  public boolean search(Point start, Point end)
  {
    long timeN = System.nanoTime();
    
    start.reachable.clear();
    end.reachable.clear();
    path.clear();
    openList.clear();

    Segment straightPath = new Segment(start, end);
    boolean intersect = false;

    for (int y = 0; y < islands.size(); y++) //straight path exists?
    {
      for (int x = 0; x < islands.get(y).edges.size(); x++)
      {
        if (straightPath.intersects(islands.get(y).edges.get(x)))
        {
          intersect = true;
          break;
        }
      }

      if (intersect)
      {
        break;
      }
    }

    if (!intersect)
    {
      start.reachable.add(end);
      end.reachable.add(start);
      path.add(start.coordinate);
      path.add(end.coordinate);
      println(System.nanoTime() - timeN);
      return true;
    }

    addReachable(start);
    addReachable(end);

    currentSearch++;

    boolean found = false;
    Point currentNode;
    start.searchID = currentSearch;
    start.hCost = start.getDistance(end);
    openList.add(start);

    while (true)
    {
      int bestIndex = -1;
      float cost = 999999999;

      if (openList.size() == 0)
      {
        found = false;
        break;
      }

      for (int k = 0; k < openList.size(); k++)
      {
        if (openList.get(k).getF() < cost)
        {
          bestIndex = k;
          cost = openList.get(k).getF();
        }
      }
      currentNode = openList.get(bestIndex);
      openList.remove(bestIndex);

      currentNode.isClosed = true;

      if (currentNode.samePlace(end))
      {
        found = true;
        break;
      }

      for (Point p : currentNode.reachable)
      {
        if (p.searchID != currentSearch)
        {
          p.parent = currentNode;
          p.hCost = p.getDistance(end);
          p.gCost = currentNode.getDistance(p) + currentNode.gCost;
          p.isClosed = false;
          p.searchID = currentSearch;
          openList.add(p);
        }
        else
        {
          if (!p.isClosed)
          {
            if (p.gCost > currentNode.gCost + currentNode.getDistance(p)) //betterCost
            {
              p.gCost = currentNode.gCost + currentNode.getDistance(p);
              p.parent = currentNode;
            }
          }
        }
      }
    }
    
    println(System.nanoTime() - timeN);

    if (found)
    {
      Point backtracking = end;

      while (backtracking != null)
      {
        path.add(backtracking.coordinate);
        backtracking = backtracking.parent;
      }
    }

    return found;
  }

  public void drawPath()
  {
    stroke(#EFF018);
    strokeWeight(3);
    for (int k = 1; k < path.size(); k++)
    {
      line(path.get(k - 1).x, path.get(k - 1).y, path.get(k).x, path.get(k).y);
    }
  }

  public void findGraph()
  {
    path.clear();
    Polygon poly;
    Point point;
    Segment seg;
    boolean intersect;
    for (int k = 0; k < islands.size(); k++) //from all islands
    {
      poly = islands.get(k);
      for (int t = 0; t < poly.vertices.size(); t++) //from all vertices
      {
        point = poly.vertices.get(t);
        point.reachable.clear();
        point.reachable.add(poly.vertices.get((t + 1) % poly.vertices.size()));
        point.reachable.add(poly.vertices.get((t - 1 + poly.vertices.size()) % poly.vertices.size()));

        for (int i = 0; i < islands.size(); i++) //to all islands
        {
          for (int j = 0; j < islands.get(i).vertices.size(); j++) //to all vertices
          {
            seg = new Segment(point, islands.get(i).vertices.get(j));
            intersect = false;
            for (int y = 0; y < islands.size(); y++) //check no other island edge intersection
            {
              for (int x = 0; x < islands.get(y).edges.size(); x++)
              {
                if (seg.intersects(islands.get(y).edges.get(x)))
                {
                  if (!seg.touches(islands.get(y).edges.get(x)))
                  {
                    intersect = true;
                    break;
                  }
                }
              }

              if (intersect)
              {
                break;
              }
            }

            if (k == i)
            {
              for (int x = 0; x < islands.get(i).intern.size(); x++) //check no internal intersection
              {
                if (seg.intersects(islands.get(i).intern.get(x)))
                {
                  if (!seg.touches(islands.get(i).intern.get(x)))
                  {
                    intersect = true;
                    break;
                  }
                }
                if (seg.is(islands.get(i).intern.get(x)))
                {
                  intersect = true;
                  break;
                }
              }
            }

            if (!intersect)
            {
              point.reachable.add(islands.get(i).vertices.get(j));
            }
          }
        }
      }
    }
  }

  public void addReachable(Point point)
  {
    Segment seg;
    boolean intersect;
    for (int i = 0; i < islands.size(); i++) //check all islands
    {
      for (int j = 0; j < islands.get(i).vertices.size(); j++) //check all vertices
      {
        seg = new Segment(point, islands.get(i).vertices.get(j));
        intersect = false;
        for (int y = 0; y < islands.size(); y++) //check no other island edge intersection
        {
          for (int x = 0; x < islands.get(y).edges.size(); x++)
          {
            if (seg.intersects(islands.get(y).edges.get(x)))
            {
              if (!seg.touches(islands.get(y).edges.get(x)))
              {
                intersect = true;
                break;
              }
            }
          }

          if (intersect)
          {
            break;
          }
        }

        if (k == i)
        {
          for (int x = 0; x < islands.get(i).intern.size(); x++) //check no internal intersection
          {
            if (seg.intersects(islands.get(i).intern.get(x)))
            {
              if (!seg.touches(islands.get(i).intern.get(x)))
              {
                intersect = true;
                break;
              }
            }
            if (seg.is(islands.get(i).intern.get(x)))
            {
              intersect = true;
              break;
            }
          }
        }

        if (!intersect)
        {
          point.reachable.add(islands.get(i).vertices.get(j));
          islands.get(i).vertices.get(j).reachable.add(point);
        }
      }
    }
  }
}