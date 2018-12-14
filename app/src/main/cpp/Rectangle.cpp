//
// Created by gamefreak on 12/13/2018.
//

#include "Rectangle.h"

Rectangle::Rectangle(int x, int y, int width, int height) {
    this->set(x, y, width, height);
}

int Rectangle::getWidth() {
    return width;
}

int Rectangle::getHeight() {
    return height;
}

int Rectangle::getX() {
    return x;
}

int Rectangle::getY() {
    return y;
}

int Rectangle::getX2() {
    return x + width;
}

int Rectangle::getY2() {
    return y + height;
}

void Rectangle::setWidth(int width) {
    this->width = width;
}

void Rectangle::setHeight(int height) {
    this->height = height;
}

void Rectangle::setX(int x) {
    this->x = x;
}

void Rectangle::setY(int y) {
    this->y = y;
}

void Rectangle::setX2(int x2) {
    if (x2 < x) {
        int t = x;
        x = x2;
        x2 = t;
    }
    this->width = x2 - x;
}

void Rectangle::setY2(int y2) {
    if (y2 < y) {
        int t = y;
        y = y2;
        y2 = t;
    }

    this->height = y2 - y;
}

void Rectangle::set(int x, int y, int width, int height) {
    this->x = x;
    this->y = y;
    this->width = width;
    this->height = height;

}

void Rectangle::cropToBounds(Rectangle rect) {
    int x1 = this->x;
    int x2 = this->x + this->width;
    int y1 = this->y;
    int y2 = this->y = this->height;

    if(x1 > rect.x) this-> x = rect.x;

    if(y1 > rect.y) this-> y = rect.y;

    if(x2 > rect.x + rect.width) {
        this-> width = rect.x + rect.width - this->x;
    }

    if(y2 > rect.y + rect.height) {
        this-> height = rect.y + rect.height - this->y;
    }

    if(this->width < 0) {
        this->width = 0;
    }

    if(this->height < 0) {
        this->height = 0;
    }

}

bool Rectangle::isEmpty() {
    return this->width == 0 || this-> height == 0;

}
