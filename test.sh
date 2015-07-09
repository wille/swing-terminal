echo -e "Default attributes\n"
echo -e "\e[1mBold"
echo -e "\e[2mDim"
echo -e "\e[4mUnderlined"
echo -e "\e[5mBlink"
echo -e "\e[7mInverted"
echo -e "\e[8mHidden"

echo -e "\nForeground colors\n"

echo -e "\e[39mDefault"
echo -e "\e[30mBlack"
echo -e "\e[31mRed"
echo -e "\e[32mGreen"
echo -e "\e[33mYellow"
echo -e "\e[34mBlue"
echo -e "\e[35mMagenta"
echo -e "\e[36mCyan"
echo -e "\e[37mLight gray"
echo -e "\e[90mDark gray"
echo -e "\e[91mLight red"
echo -e "\e[92mLight green"
echo -e "\e[93mLight yellow"
echo -e "\e[94mLight blue"
echo -e "\e[95mLight magenta"
echo -e "\e[96mLight cyan"
echo -e "\e[97mWhite"

echo -e "\nBackground colors\n"

echo -e "\e[49mDefault"
echo -e "\e[40mBlack"
echo -e "\e[41mRed"
echo -e "\e[42mGreen"
echo -e "\e[43mYellow"
echo -e "\e[44mBlue"
echo -e "\e[45mMagenta"
echo -e "\e[46mCyan"
echo -e "\e[47mLight gray"
echo -e "\e[100mDark gray"
echo -e "\e[101mLight red"
echo -e "\e[102mLight green"
echo -e "\e[103mLight yellow"
echo -e "\e[104mLight blue"
echo -e "\e[105mLight magenta"
echo -e "\e[106mLight cyan"
echo -e "\e[107mWhite"

echo -e "\nOther tests\n"

echo -e "\e[1;31;42mGreen background, red foreground, bold\e[0m"
echo -e "\e[1;4mBold and Underlined"

for clbg in {40..47} {100..107} 49 ; do
	#Foreground
	for clfg in {30..37} {90..97} 39 ; do
		#Formatting
		for attr in 0 1 2 4 5 7 ; do
			#Print the result
			echo -en "\e[${attr};${clbg};${clfg}m[${attr};${clbg};${clfg}m \e[0m"
		done
		echo #Newline
	done
done