from pytesseract import *
from PIL import Image
import cv2


def convert_color_black_and_white(img, file_name):
    img2 = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    cv2.imwrite(f"{file_name}_BNW.jpg", img2)
    return img2

def make_image_to_string_BNW(file_name):

    img = cv2.imread(f"{file_name}.jpg", cv2.IMREAD_COLOR)
    
    img2 = convert_color_black_and_white(img, file_name)

    text = image_to_string(img2, lang='eng')

    with open(f"{file_name}_BNW.txt", "w") as text_file:
        text_file.write(text)


def make_image_to_string(file_name):
    img = cv2.imread(f"{file_name}.jpg", cv2.IMREAD_COLOR)

    text = image_to_string(img, lang='eng')

    with open(f"{file_name}.txt", "w") as text_file:
        text_file.write(text)