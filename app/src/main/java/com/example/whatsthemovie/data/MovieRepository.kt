package com.example.whatsthemovie.data

import com.example.whatsthemovie.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class MovieRepository(private val movieDao: MovieDao) {
    fun getRandomMovieExcluding(excludeIds: List<Int>): Flow<MovieEntity?> = flow {
        val movies = movieDao.getAllMovies().filter { it.id !in excludeIds }
        if (movies.isNotEmpty()) {
            val randomIndex = (0 until movies.size).random()
            emit(movies[randomIndex])
        } else {
            emit(null)
        }
    }

    //метод для получения 3 случайных вариантов
    suspend fun getRandomOptionsForMovie(currentMovieId: Int): List<String> {
        val otherMovies = movieDao.getRandomOtherMovies(currentMovieId)
        return otherMovies.map { it.name }
    }

    suspend fun initializeMovies() {
        if (movieDao.getCount() == 0) {
            val movies = createMovieList()
            movieDao.insertAll(movies)
        }
    }

    private fun createMovieList(): List<MovieEntity> {
        return listOf(
            MovieEntity(
                imageId = R.drawable.avatar,
                name = "Аватар",
                quote = "В этом теле я чувствую себя более живым, чем когда-либо прежде.",
                musicId = R.raw.avatar
            ),
            MovieEntity(
                imageId = R.drawable.inception,
                name = "Начало",
                quote = "Говорят, мы используем лишь часть истинного потенциала нашего мозга, но это когда мы бодрствуем. Когда же мы спим, наш мозг способен практически на всё.",
                musicId = R.raw.inception
            ),
            MovieEntity(
                imageId = R.drawable.interstellar,
                name = "Интерстеллар",
                quote = "Любовь - это единственное доступное нам чувство, способное выйти за пределы времени и пространства.",
                musicId = R.raw.interstellar
            ),
            MovieEntity(
                imageId = R.drawable.avengers,
                name = "Мстители",
                quote = " — Без обид, но я не работаю в команде.\n" +
                        "— Да. Парень в бронированном костюме. А снять - кто ты без него?\n" +
                        "— Гений, миллиардер, плэйбой, филантроп.",
                musicId = R.raw.avengers
            ),
            MovieEntity(
                imageId = R.drawable.transformers,
                name = "Трансформеры",
                quote = "Люди не должны расплачиваться за наши ошибки. Служить рядом с вами - честь для меня.",
                musicId = R.raw.transformers
            ),
            MovieEntity(
                imageId = R.drawable.titanic,
                name = "Титаник",
                quote = "Прыгнешь ты, прыгну и я, так?",
                musicId = R.raw.titanic
            ),
            MovieEntity(
                imageId = R.drawable.pirats,
                name = "Пираты Карибского моря:Проклятие Чёрной жемчужины",
                quote = "Если ты ждал подходящего момента... то это был он.",
                musicId = R.raw.pirats
            ),
            MovieEntity(
                imageId = R.drawable.harrypotter,
                name = "Гарри Поттер и философский камень",
                quote = "Требуется немалое мужество, чтобы противостоять врагам. Но еще больше мужества нужно, чтобы противостоять друзьям.",
                musicId = R.raw.harrypotter
            ),
            MovieEntity(
                imageId = R.drawable.lordoftherings,
                name = "Властелин колец: Братство кольца",
                quote = "Моя собственность. Моя любовь. Моя... моя прелесть.",
                musicId = R.raw.lordoftherings
            ),
            MovieEntity(
                imageId = R.drawable.thehobbit,
                name = "Хоббит: Нежданное путешествие",
                quote = "Мир вовсе не в твоих книгах и картах, он вон там - за окном!",
                musicId = R.raw.thehobbit
            ),
            MovieEntity(
                imageId = R.drawable.illusion,
                name = "Иллюзия обмана",
                quote = " Смотрите внимательно. Потому что чем вы ближе, тем меньше вы видите.",
                musicId = R.raw.illusion
            ),
            MovieEntity(
                imageId = R.drawable.passengers,
                name = "Пассажиры",
                quote = "Перестань волноваться о том, что ты не можешь контролировать. Просто живи.",
                musicId = R.raw.passengers
            ),
            MovieEntity(
                imageId = R.drawable.topgun,
                name = "Топ Ган: Мэверик",
                quote = "Дело не в самолете, а в пилоте.",
                musicId = R.raw.topgun
            ),
            MovieEntity(
                imageId = R.drawable.hungergames,
                name = "Голодные игры",
                quote = " Вы не хуже меня знаете, как управлять толпой. Если толпу нельзя запугать, толпе надо во что-то верить.",
                musicId = R.raw.hungergames
            ),
            MovieEntity(
                imageId = R.drawable.pearlharbor,
                name = "Перл-Харбор",
                quote = "Я здесь, чтобы быть пилотом. А в полете буквы ни к чему, там все дело в скорости - когда ты ощущаешь самолет как часть своего собственного тела. Мэм, пожалуйста, верните мне крылья.",
                musicId = R.raw.pearlharbor
            ),
            MovieEntity(
                imageId = R.drawable.armageddon,
                name = "Армагеддон",
                quote = "Так уже было. Так ещё будет. Это лишь вопрос времени.",
                musicId = R.raw.armageddon
            ),
            MovieEntity(
                imageId = R.drawable.proposal,
                name = "Предложение",
                quote = "Есть причина моего одиночества - мне так спокойнее.",
                musicId = R.raw.proposal
            ),
            MovieEntity(
                imageId = R.drawable.twilight,
                name = "Сумерки",
                quote = "На перемены в твоём настроении у меня аллергия.",
                musicId = R.raw.twilight
            ),
            MovieEntity(
                imageId = R.drawable.shutterisland,
                name = "Остров проклятых",
                quote = "Как думаешь, что лучше, жить монстром или умереть человеком?",
                musicId = R.raw.shutterisland
            ),
            MovieEntity(
                imageId = R.drawable.bodyguard,
                name = "Телохранитель",
                quote = "Сделайте мне одолжение, никогда не делайте мне одолжений!",
                musicId = R.raw.bodyguard
            ),
            MovieEntity(
                imageId = R.drawable.venom,
                name = "Веном",
                quote = "Это не ты нашел нас, это мы нашли тебя. Ты типа моей личной тачки.",
                musicId = R.raw.venom
            ),
            MovieEntity(
                imageId = R.drawable.meninblack,
                name = "Люди в чёрном",
                quote = "Знаешь, в чем между нами разница? Костюм мне идёт!",
                musicId = R.raw.meninblack
            ),
            MovieEntity(
                imageId = R.drawable.ironman,
                name = "Железный человек",
                quote = "Я живу в долг ради одной цели. Я не безумец, Пеппер. Я наконец-то понял, что я должен делать, и сердце подсказывает, я прав.",
                musicId = R.raw.ironman
            ),
            MovieEntity(
                imageId = R.drawable.guardiansofthegalaxy,
                name = "Стражи Галактики",
                quote = "Я поражаюсь. Как в пекло лезть, так он спит. Как деньги делить - сразу зашелестел.",
                musicId = R.raw.guardiansofthegalaxy
            ),
            MovieEntity(
                imageId = R.drawable.stepup,
                name = "Шаг вперёд",
                quote = "Столько ошибок, сколько я наделал, хватило бы на целую жизнь. Если хочешь чего-то добиться, надо бороться за это. И я приехал сюда, чтобы впервые в жизни бороться за то, что мне важно!",
                musicId = R.raw.stepup
            ),
            MovieEntity(
                imageId = R.drawable.stepup2thestreets,
                name = "Шаг вперёд 2: Улицы",
                quote = "Вы нас учите, что танцем нужно что-то говорить, так? Я думаю, сейчас многие наконец-то решили высказаться.",
                musicId = R.raw.stepup2thestreets
            ),
            MovieEntity(
                imageId = R.drawable.thegentlemen,
                name = "Джентльмены",
                quote = "Чтобы стать царем зверей, мало вести себя по-царски. Надо быть царём. Нельзя сомневаться. Сомнения порождают хаос и ведут тебя к гибели.",
                musicId = R.raw.thegentlemen
            ),
            MovieEntity(
                imageId = R.drawable.jumanji,
                name = "Джуманджи: Зов джунглей",
                quote = "— Народ, этого просто не может быть! Может, мы все... в коме?\n" +
                        "— Сдурела?! В кому вместе не впадают!",
                musicId = R.raw.jumanji
            )


        )
    }
}