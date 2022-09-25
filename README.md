# Slicer
A 3D printer slicer software built from scratch. Currently a work in progress. 

What is a slicer? A slicer takes in a 3D model (STL file), "slices" it into a bunch of horizontal layers, and generates a toolpath (in GCODE) from these newly created 2D layers. The toolpath is the path that the 3d printer takes to lay down the plastic. Note: 3D printing is a bit of a misnomer. It would more accurately be described as 2.5D printing, as at no point are all 3 axes moving. It lays down plastic in XY, and Z advances to the next layer when done. 

To do list:
1) ~~Parse STL file into some usable data~~
2) ~~Slice the model, creates *n* amount of 2D layers~~
3) ~~Generate toolpath from 2D layers~~
4) Convert the toolpath into GCODE
5) Interactive GUI

## How it works:
First, we must understand what an STL file is. From Wikipedia: 
> An STL file describes a raw, unstructured triangulated surface by the unit normal and vertices (ordered by the right-hand rule) of the triangles using a three-dimensional Cartesian coordinate system.

<img src="https://cdn2.sculpteo.com/blog/wp-content/uploads/2019/06/uT6do-min.jpg" width="400">

### How to slice:
Since the triangle mesh only describes the surface of the model, an STL is essentially hollow. Knowing this, you can easily slice a triangle mesh to get the perimeter of the model. In a 2D coordinate system, if you take a triangle and draw a straight line through it, the line will always intersect two edges. The same holds true in 3D. If you take a triangle that is defined in 3D space, and you intersect it with a plane, the plane will intersect two edges (the only exception is when the triangle is parallel to the plane). Since all triangles are adjacent to eachother, if you find all triangle-plane intersection points and connect them with lines, you will have the perimieter. The drawing below demonstrates this. The dotted lines on the left represent the horizontal plane intersecting the model. 


![Screen Shot 2022-09-09 at 10 37 26 PM](https://user-images.githubusercontent.com/43012097/189470809-6c93e771-a211-4c05-a1e0-c58d714b467c.png)

### Toolpath algorithm:
One we have completed the previous step, we must somehow use this perimeter to come up with a path that will cover every square inch of the 2D triangle. Since this triangle shape is only defined by 3 points, we do not have much information to go off of. The way most printers travel going horizontally back and forth, zigzagging downward. To do this, we must "slice" the 2D layers horizontally at set intervals (the width of the nozzle that extrudes plastic), and calculate where the horizontal lines intersect the perimter. Once we have these intersection points, to generate the toolpath we just need to draw straight, horizontal lines between these points. A simplified example shown below: 

![Screen Shot 2022-09-09 at 11 10 23 PM](https://user-images.githubusercontent.com/43012097/189471630-20f028e5-2eec-4bef-8228-d0435e832c08.png)

### Generating GCODE:
To be continued...


