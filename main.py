from image_to_string import *


# for index in range(1,5):
#     file_name = index
#     image_to_string(file_name)

for index in range(1,18):
    file_name = index
    #convert_image_to_string_file(file_name)

    text = convert_image_to_string(file_name)
    print(f'================{index}================')
    print('text')
    print(text)
    print('password')
    password = get_password(text)
    print(password)
    print('====================================')
    print()

# file_name = input('input file name: ')
# make_image_to_string(file_name)
# make_image_to_string_BNW(file_name)

