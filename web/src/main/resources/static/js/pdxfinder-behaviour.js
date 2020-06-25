class PDXFinder {

    static switchButtonText(targetId) {

        const  text1 = "SHOW ABSTRACT";
        const text2 = "HIDE ABSTRACT";

        let targetElement = document.querySelector(`${targetId}`);
        let nowText = targetElement.innerHTML.trim();
        switch(nowText) {
            case text1:
                targetElement.innerHTML  = text2;
                break;
            case text2:
                targetElement.innerHTML  = text1;
                break;
            default:
        }
    }

}

