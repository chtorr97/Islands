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

void setup()
{
  size(800, 800);

  ps = new ArrayList<Polygon>();
  ps.add(new Polygon());

  start = new Point(20, 20);
  end = new Point(780, 780);

  pf = new Pathfinder();
  
  drawAll();
}

void draw()
{
  drawAll();
}

void mouseReleased()
{
  noStroke();
  ps.get(k).vertices.add(new Point(mouseX, mouseY));
  fill(0, 0, 255);
  ellipse(mouseX, mouseY, 5, 5);
}

void keyPressed()
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

void drawAll()
{
  background(#21EAFA);

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
  fill(#148E35);
  ellipse(start.coordinate.x, start.coordinate.y, 15, 15);

  noStroke();
  fill(0, 0, 255);
  ellipse(end.coordinate.x, end.coordinate.y, 15, 15);

  pf.drawPath();
}