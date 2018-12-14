//
// Created by gamefreak on 12/13/2018.
//

#ifndef DOCUMENTSCANNER_RECTANGLE_H
#define DOCUMENTSCANNER_RECTANGLE_H
class  Rectangle {
private:
    int width;
    int height;
    int x;
    int y;
public:
    Rectangle(int x, int y, int width, int height);
    int getWidth();
    int getHeight();
    int getX();
    int getY();
    int getX2();
    int getY2();
    void setX(int x);
    void setY(int y);
    void setX2(int x2);
    void setY2(int y2);
    void setWidth(int width);
    void setHeight(int height);
    void set(int x, int y, int width, int height);
    void cropToBounds(Rectangle rect);
    bool isEmpty();
};


#endif //DOCUMENTSCANNER_RECTANGLE_H
