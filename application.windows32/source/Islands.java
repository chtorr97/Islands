import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Islands extends PApplet {

/*
COMMANDS:
 s - Set star point
 e - Set end point
 i - Compute islands
 p - Show paths
 t - Show triangles
 */

ArrayList<Polygon> ps;

Pathfinder pf;

Point start, end;

int k = 0;

boolean NODES = false;
boolean TRIANGULATION = false;

public void setup()
{
  

  ps = new ArrayList<Polygon>();
  ps.add(new Polygon());

  start = new Point(20, 20);
  end = new Point(780, 780);

  pf = new Pathfinder();
  
  drawAll();
}

public void draw()
{
  drawAll();
}

public void mouseReleased()
{
  noStroke();
  ps.get(k).vertices.add(new Point(mouseX, mouseY));
  fill(0, 0, 255);
  ellipse(mouseX, mouseY, 5, 5);
}

public void keyPressed()
{
  if (key == 'i')
  {
    ps.get(k).connect();
    ps.get(k).triangulate();
    pf.islands.add(ps.get(k));
    pf.findGraph();
    pf.search(start, end);
    ps.add(new Polygon());
    k++;
  }
  if (key == 's')
  {
    start.coordinate = new PVector(mouseX, mouseY);
    pf.findGraph();
    pf.search(start, end);
  }
  if (key == 'e')
  {
    end.coordinate = new PVector(mouseX, mouseY);
    pf.findGraph();
    pf.search(start, end);
  }
  if (key == 'p')
  {
    NODES = !NODES;
  }
  if (key == 't')
  {
    TRIANGULATION = !TRIANGULATION;
  }
}

public void drawAll()
{
  background(0xff21EAFA);

  for (Polygon p : ps)
  {
    stroke(0);
    strokeWeight(5);
    fill(0, 255, 0);
    beginShape();
    for (Point v : p.vertices)
    {
      vertex(v.coordinate.x, v.coordinate.y);
    }
    endShape(CLOSE);
    strokeWeight(1);

    if (TRIANGULATION)
    {
      strokeWeight(5);
      for (Segment s : p.intern)
      {
        line(s.first.coordinate.x, s.first.coordinate.y, s.second.coordinate.x, s.second.coordinate.y);
      }
    }
    strokeWeight(1);

    stroke(255, 0, 0);
    if (NODES)
    {
      for (Point a : p.vertices)
      {
        for (Point b : a.reachable)
        {
          line(a.coordinate.x, a.coordinate.y, b.coordinate.x, b.coordinate.y);
        }
      }
    }
  }

  if (NODES)
  {
    for (Point p : start.reachable)
    {
      line(start.coordinate.x, start.coordinate.y, p.coordinate.x, p.coordinate.y);
    }

    for (Point p : end.reachable)
    {
      line(end.coordinate.x, end.coordinate.y, p.coordinate.x, p.coordinate.y);
    }
  }
  
  noStroke();
  fill(0xff148E35);
  ellipse(start.coordinate.x, start.coordinate.y, 15, 15);

  noStroke();
  fill(0, 0, 255);
  ellipse(end.coordinate.x, end.coordinate.y, 15, 15);

  pf.drawPath();
}
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
    stroke(0xffEFF018);
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
  public void settings() {  size(800, 800); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Islands" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
