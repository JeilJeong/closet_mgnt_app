from selenium import webdriver

def get_useragent():
    # ---------------------------headless part-----------------------------
    options = webdriver.ChromeOptions()
    options.headless = True
    # background에서 돌아가는 chrome에서 보이지 않지만 얼만큼의 size로 띄울 것인지 options.add_argument()로 결정
    options.add_argument("window-size=1920x1080")
    # ----------------------for getting user-agent-------------------------

    browser = webdriver.Chrome(options=options)
    browser.maximize_window()

    url = "https://www.whatismybrowser.com/detect/what-is-my-user-agent"
    browser.get(url)

    detected_value = browser.find_element_by_id("detected_value")
    list = str()
    list = detected_value.text
    list.replace("HeadlessChrome", "Chrome")
    print("1." + list)
    browser.quit()
    # ---------------------------------------------------------------------
    return (list)
