# Slicer
A 3D printer slicer software built from scratch. Currently a work in progress. 

What is a slicer? A slicer takes in a 3D model (STL file), "slices" it into a bunch of horizontal layers, and generates a toolpath (in GCODE) from these newly created 2D layers. The toolpath is the path that the 3d printer takes to lay down the plastic. Note: 3D printing is a bit of a misnomer. It would more accurately be described as 2.5D printing, as at no point are all 3 axes moving. It lays down plastic in XY, and Z advances to the next layer when done. 

To do list:
1) ~~Parse STL file into some usable data~~
2) ~~Slice the model, creates *n* amount of 2D layers~~
3) ~~Generate toolpath from 2D layers~~
4) Convert the toolpath (currently in the form of 2d rays) into GCODE
5) Interactive GUI

## How it works:
First, we must understand what an STL file is. From Wikipedia: 
> An STL file describes a raw, unstructured triangulated surface by the unit normal and vertices (ordered by the right-hand rule) of the triangles using a three-dimensional Cartesian coordinate system.

<img src="https://cdn2.sculpteo.com/blog/wp-content/uploads/2019/06/uT6do-min.jpg" width="400">
