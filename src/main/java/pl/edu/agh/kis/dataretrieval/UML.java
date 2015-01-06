package pl.edu.agh.kis.dataretrieval;

/**
 * PlantUML Diagrams 
 * 
 * Use Cases
 * @startuml

:Urzutkownik: as user
:Baza danych: as db
:Strona internetowa: as site
left to right direction
rectangle "System do pozyskiwania informacji ze stron WWW" {
	(U�ycie trybu okienkowego) as windowMode
	(U�ycie trybu konsolowego) as consoleMode
	user -right-> windowMode
	windowMode ..> (Dodanie nowej konfiguracji) : <<include>>
	windowMode ..> (Utworzenie nowej konfiguracji) : <<include>>
	windowMode ..> (Edycja wybranej konfiguracji) : <<include>>
	windowMode ..> (Wy�wietlenie wybranej konfiguracji) : <<include>>
	windowMode ..> (Sprawdzenie sk�adni wybranej konfiguracji) : <<include>>
	windowMode ..> (Usuni�cie wybranej konfiguracji) : <<include>>
	
	(Wydobywanie informacji) as processing
	(Wydobywanie w trybie standardowym) as standard
	(Wydobywanie w trybie masowym) as bulk
	windowMode .> processing : <<include>>
	processing <.. standard: <<extend>>
	processing <.. bulk : <<extend>>

	db -- processing 
	
	
	user --> consoleMode
	consoleMode ..> bulk : <<include>>
	
	usecase standardForm as "Pobranie danych na podstawie
	jednego ustawienia p�l w formularzach"
	standardForm .> standard : <<extend>>
	
	usecase bulkAntyBot as "Pobranie danych partiami
	w odst�pach czasowych"
	usecase bulkMultiValues as "Pobranie danych na podstawie
	r�nych ustawie� p�l w formularzach"
	bulkAntyBot .> bulk : <<extend>>
	bulk <. bulkMultiValues : <<extend>>
}

@enduml

 * Sequence
@startuml
actor U�ytkownik as user
participant "Interfejs\ngraficzny" as gui
participant "Przetwarzanie\nkonfiguracji" as config
participant "Przeszukiwanie\nstron" as crawl
participant "Wydobywanie\ninformacji" as retr
participant "Interfejs\nbazodanowy" as db

alt tryb okienkowy
	user -> gui : uruchomienie\nprogramu
	gui -> gui : wyb�r konfiguracji
	gui -> config : przekazanie\nkonfiguracji\ndo przetwarzania
	gui -> gui :otworzenie okienka\nz post�pem procesu
else tryb konsolowy
	user -> config : uruchomienie programu i przekazanie\n konfiguracji do przetwarzania
end
config -> config : sprawdzenie sk�adni
config -> config : wczytanie obydwu\nkonfiguracji
config -> crawl : przekazanie\nkonfiguracji\ndo wyszukiwania
activate crawl
loop Ilo�� alternatywnych ustawie� formularzy w trybie masowym, albo maksymalna ilo�� rekord�w\nW trybie standardowym jedno przej�cie
	alt tryb masowy
		crawl -> crawl : ustawienie wartosci\nformularzy ustawianych\nw danej iteracji
	end
	group dla ka�dego elementu w konfiguracji wyszukiwania
		alt link
			crawl -> crawl : przej�cie do linku
		else formularz
			alt tryb masowy
				crawl -> crawl :automatyczne uzupe�nienie
			else tryb standardowy
				crawl -> gui :przeslanie formularza do uzupelnienia
				activate gui
				gui -> user :wyswietlenie formularza
				user --> gui :uzupe�nienie formularza
				gui --> crawl :przeslanie uzupelnionego formularza
				deactivate gui
			end
		else strona z wynikami
			config -> retr : przekazanie\nkonfiguracji\ndo wydobywania
			loop do pobrania wszystkich stron,\nalbo do maksymalnej ilo�ci stron
				crawl -> retr :przekazanie kolejnej strony
				activate retr
				retr -> retr :wydobycie\ndanych\nze strony
				retr -> db :przeslanie\nrekordu\ndo zapisania
				db -> db :zapisanie\nrekordu\nw bazie
				db --> retr
				retr --> crawl
				deactivate retr
			end
		else alternatywa zale�na od uzupe�nionych warto�ci formularza
			crawl -> crawl :wyb�r odpowiedniej\ngrupy kolejnych element�w
		end
		
	end
end
alt tryb masowy
	crawl -> config :zpisanie stanu\npobierania
end
crawl --> user :zwr�cenie komunikatu o zako�czeniu wydobywania
deactivate crawl
@enduml

 * @author Mikolaj
 *
 */
public class UML {

}
