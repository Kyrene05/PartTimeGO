package com.example.parttimego.data.location

object LocationData {

    val states = listOf(
        "Perlis",
        "Kedah",
        "Pulau Pinang",
        "Perak",
        "Selangor",
        "Kuala Lumpur",
        "Melaka",
        "Johor",
        "Kelantan",
        "Pahang",
        "Terengganu",
        "Negeri Sembilan",
        "Sabah",
        "Sarawak",
        "Putrajaya",
        "Labuan"
    )

    val areasByState = mapOf(

        "Perlis" to listOf(
            "Kangar",
            "Arau",
            "Kuala Perlis",
            "Padang Besar",
            "Simpang Empat",
            "Kaki Bukit",
            "Beseri"
        ),

        "Kedah" to listOf(
            "Alor Setar",
            "Sungai Petani",
            "Kulim",
            "Langkawi (Kuah)",
            "Jitra",
            "Baling",
            "Pokok Sena",
            "Yan",
            "Pendang",
            "Sik",
            "Kuala Nerang"
        ),

        "Pulau Pinang" to listOf(
            "George Town",
            "Tanjong Bungah",
            "Bayan Lepas",
            "Air Itam",
            "Butterworth",
            "Bukit Mertajam",
            "Perai",
            "Batu Kawan",
            "Seberang Jaya",
            "Kepala Batas",
            "Nibong Tebal"
        ),

        "Perak" to listOf(
            "Ipoh",
            "Taiping",
            "Teluk Intan",
            "Seri Manjung",
            "Kampar",
            "Sitiawan",
            "Kuala Kangsar",
            "Batu Gajah",
            "Lumut",
            "Tapah",
            "Tanjung Malim",
            "Parit Buntar"
        ),

        "Selangor" to listOf(
            "Petaling Jaya",
            "Shah Alam",
            "Subang Jaya",
            "Puchong",
            "Klang",
            "Ampang",
            "Kajang",
            "Cyberjaya",
            "Rawang",
            "Selayang",
            "Seri Kembangan",
            "Bangi",
            "Semenyih",
            "Banting",
            "Sepang",
            "Kuala Selangor",
            "Sekinchan"
        ),

        "Kuala Lumpur" to listOf(
            "Bukit Bintang",
            "Bangsar",
            "Cheras",
            "Kepong",
            "Mont Kiara",
            "Setapak",
            "Brickfields",
            "Wangsa Maju",
            "Segambut",
            "Sri Petaling",
            "Bukit Jalil",
            "Sentul",
            "Batu Caves (KL portion)"
        ),

        "Melaka" to listOf(
            "Bandaraya Melaka (Banda Hilir)",
            "Ayer Keroh",
            "Alor Gajah",
            "Jasin",
            "Batu Berendam",
            "Masjid Tanah",
            "Klebang",
            "Bukit Katil",
            "Merlimau"
        ),

        "Johor" to listOf(
            "Johor Bahru",
            "Iskandar Puteri",
            "Pasir Gudang",
            "Kulai",
            "Batu Pahat",
            "Muar",
            "Kluang",
            "Kota Tinggi",
            "Segamat",
            "Pontian",
            "Tangkak",
            "Mersing"
        ),

        "Kelantan" to listOf(
            "Kota Bharu",
            "Pasir Mas",
            "Tumpat",
            "Tanah Merah",
            "Machang",
            "Pasir Puteh",
            "Kuala Krai",
            "Gua Musang",
            "Bachok",
            "Jeli"
        ),

        "Pahang" to listOf(
            "Kuantan",
            "Temerloh",
            "Bentong",
            "Cameron Highlands (Tanah Rata / Brinchang)",
            "Raub",
            "Jerantut",
            "Pekan",
            "Mentakab",
            "Kuala Lipis",
            "Rompin"
        ),

        "Terengganu" to listOf(
            "Kuala Terengganu",
            "Chukai (Kemaman)",
            "Dungun",
            "Besut (Jertih)",
            "Marang",
            "Kuala Berang",
            "Setiu"
        ),

        "Negeri Sembilan" to listOf(
            "Seremban",
            "Nilai",
            "Port Dickson",
            "Senawang",
            "Bahau",
            "Kuala Pilah",
            "Tampin",
            "Rembau"
        ),

        "Sabah" to listOf(
            "Kota Kinabalu",
            "Sandakan",
            "Tawau",
            "Lahad Datu",
            "Penampang",
            "Keningau",
            "Semporna",
            "Tuaran",
            "Papar",
            "Kudat",
            "Ranau"
        ),

        "Sarawak" to listOf(
            "Kuching",
            "Miri",
            "Sibu",
            "Bintulu",
            "Kota Samarahan",
            "Sri Aman",
            "Limbang",
            "Sarikei",
            "Kapit",
            "Mukah"
        ),

        "Putrajaya" to listOf(
            "Precinct 1 (Government Complex)",
            "Precinct 2 & 3 (Boulevard / Civic Center)",
            "Precinct 4 (Commercial District)",
            "Precinct 8 & 9 (Residential Areas)",
            "Precinct 11 (Residential / Saujana Hijau)",
            "Precinct 14 & 15 (Diplomatic Enclave / Commercial)",
            "Precinct 16 (Alamanda / Commercial)"
        ),

        "Labuan" to listOf(
            "Victoria (Town Centre)",
            "Layang-Layangan",
            "Kampung Sungai Bedaun",
            "Batu Manikar",
            "Rancha-Rancha",
            "Pohon Batu"
        )
    )

    fun getAreas(state: String): List<String> {
        return areasByState[state] ?: emptyList()
    }
}