from image_to_string import *


# for index in range(1,5):
#     file_name = index
#     image_to_string(file_name)

correct_text_ount = 0
wrong_text_count = 0
for index in range(1,18):
    file_name = index

    # convert image to text
    # text = convert_image_to_string(file_name)
    # bnw_text = convert_image_to_string(f'bnw/{file_name}_BNW')
    # cut_text = convert_image_to_string(f'cut/{file_name}')
    cut_bnw_text = convert_image_to_string_BNW(f'cut/{file_name}')
    
    text = cut_bnw_text
    # print
    print(f'================{index}================')
    print(f'text:{text}')

    password = get_password(text)
    if(len(password) == 0):
        password = ''
    else:
        password = password[0]

    print(f'password: {password}')

    if(is_answer(file_name, password) == True):
        correct_text_ount += 1
    else:
        wrong_text_count += 1
    
    
    print('====================================')
    print()

# file_name = input('input file name: ')
# make_image_to_string(file_name)
# make_image_to_string_BNW(file_name)

print(f'correct_text: {correct_text_ount}')
print(f'wrong_text: {wrong_text_count}')